package net.tommay.sudoku

import scala.util.Random

object Creater {
  // Returns (puzzle, solvedPuzzle)

  def createWithSolution(
    rnd: Random,
    layout: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : (Puzzle, Puzzle) =
  {
    val (rnd1, rnd2) = Util.split(rnd)
    val solvedPuzzle = randomSolvedPuzzle(rnd1)
    val shuffledLayout = Util.shuffle(layout, rnd2)
    val puzzle = createFromSolved(solvedPuzzle, shuffledLayout, solveFunc)
    (puzzle, solvedPuzzle)
  }

  def create(
    rnd: Random,
    layout: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : Puzzle =
  {
    createWithSolution(rnd, layout, solveFunc)._1
  }

  // Start with a solved Puzzle and remove sets of cells (that will
  // result in a specific type of layout) which leave a Puzzle which
  // is uniquely solvable by the given solver function.

  def createFromSolved(
    puzzle: Puzzle,
    cellNumberLists: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : Puzzle =
  {
    cellNumberLists.foldLeft(puzzle) {case (accumPuzzle, cellNumbers) =>
      // We know accumPuzzle has only one solution.  Remove
      // cellNumbers and check whether that's still true.
      val newPuzzle = accumPuzzle.remove(cellNumbers)
      solveFunc(newPuzzle) match {
        case Stream(_) =>
          // newPuzzle has only one solution, go with it.
          newPuzzle
        case _ =>
          // Ooops, removed too much, stick with the original.
          accumPuzzle
      }
    }
  }

  def createStream(
    rnd: Random,
    layout: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : Stream[Puzzle] =
  {
    val (rnd1, rnd2) = Util.split(rnd)
    create(rnd1, layout, solveFunc) #:: createStream(rnd2, layout, solveFunc)
  }

  def randomSolvedPuzzle(rnd: Random) : Puzzle = {
    val emptyPuzzle = Puzzle.empty
    val randomSolution = Solver.allRandomSolutions(rnd)(emptyPuzzle).head
    randomSolution.puzzle
  }
}

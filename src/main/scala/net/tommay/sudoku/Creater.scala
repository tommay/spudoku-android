package net.tommay.sudoku

import scala.util.Random

object Creater {
  def create(
    rnd: Random,
    layout: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : Puzzle =
  {
    createWithSolution(rnd, layout, solveFunc)._1
  }

  def createWithSolution(
    rnd: Random,
    layout: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : (Puzzle, Solution) =
  {
    val (rnd1, rnd2) = Util.split(rnd)
    val solvedPuzzle = randomSolvedPuzzle(rnd1)
    val shuffledLayout = Util.shuffle(layout, rnd2)
    createFromSolved(solvedPuzzle, shuffledLayout, solveFunc)
  }

  // Start with a solved Puzzle and remove sets of cells (that will
  // result in a specific type of layout) which leave a Puzzle which
  // is uniquely solvable by the given solver function.

  def createFromSolved(
    puzzle: Puzzle,
    cellNumberLists: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : (Puzzle, Solution) =
  {
    val initAccum = (puzzle, Solution(puzzle, Iterable.empty))
    cellNumberLists.foldLeft(initAccum) {case (accum, cellNumbers) =>
      // We know accum's puzzle has only one solution.  Remove
      // cellNumbers and check whether that's still true.
      val oldPuzzle = accum._1
      val newPuzzle = oldPuzzle.remove(cellNumbers)
      solveFunc(newPuzzle) match {
        case Stream(solution) =>
          // newPuzzle has only one solution, go with it.
          (newPuzzle, solution)
        case _ =>
          // Ooops, removed too much, stick with the original.
          accum
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

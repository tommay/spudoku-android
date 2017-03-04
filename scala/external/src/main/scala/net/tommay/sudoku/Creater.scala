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
    createStreamWithSolution(rnd, layout, solveFunc).head
  }

  def createStreamWithSolution(
    rnd: Random,
    layout: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : Stream[(Puzzle, Solution)] =
  {
    val (rndThis, rndNext) = Util.split(rnd)
    val (rnd1, rnd2) = Util.split(rndThis)
    val solvedPuzzle = randomSolvedPuzzle(rnd1)
    val shuffledLayout = Util.shuffle(layout, rnd2)
    createFromSolved(solvedPuzzle, shuffledLayout, solveFunc) #::
      createStreamWithSolution(rndNext, layout, solveFunc)
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

  def randomSolvedPuzzle(rnd: Random) : Puzzle = {
    val emptyPuzzle = Puzzle.empty
    val randomSolution = Solver.allRandomSolutions(rnd)(emptyPuzzle).head
    randomSolution.puzzle
  }
}

package net.tommay.sudoku

import scala.util.Random

// Provide a "create" function intended to be called from Java that
// does all the heavy lifting like Random creation and Layout lookup
// in Scala.

object CreaterForJava {
  def createEasy(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    // EasyPeasy and MissingOne are both subsets of Needed, but they
    // are the easiest subsets of Needed to find visually.  The same
    // puzzles will be created no matter what the order is, but put
    // MissingOne first because it's faster.a

    val options = new SolverOptions(
      List(Heuristic.MissingOne, Heuristic.EasyPeasy), false, false)
    create(randomSeed, layoutName, options)
  }

  def create(randomSeed: Int, layoutName: String, options: SolverOptions)
      : (String, String) =
  {
    val rnd = new scala.util.Random(randomSeed)
    Layout.getLayout(layoutName) match {
      case None => ("", "")
      case Some(layout) =>
        val (puzzle, solution) = Creater.createWithSolution(
          rnd, layout, Solver.solutions(options))
        (puzzle.toString, solution.puzzle.toString)
    }
  }

  def createWicked(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    val rnd = new Random(randomSeed)
    val options = new SolverOptions(
      List(Heuristic.Forced, Heuristic.Needed, Heuristic.Tricky), false, true)
    val solveFunc = Solver.solutions(options)(_)
    Layout.getLayout(layoutName) match {
      case None => ("", "")
      case Some(layout) =>
        val puzzles = createStreamWithSolution(rnd, layout, solveFunc)
        val puzzlesRequiringGuessing = puzzles.filter{case (_, solution) =>
          solution.steps.exists(_.tjpe == Heuristic.Guess)
        }
        val (puzzle, solution) = puzzlesRequiringGuessing.head
        (puzzle.toString, solution.puzzle.toString)
    }
  }

  def createStreamWithSolution(
    rnd: Random,
    layout: Iterable[Iterable[Int]],
    solveFunc: Puzzle => Stream[Solution])
      : Stream[(Puzzle, Solution)] =
  {
    val (rnd1, rnd2) = Util.split(rnd)
    Creater.createWithSolution(rnd1, layout, solveFunc) #::
      createStreamWithSolution(rnd2, layout, solveFunc)
  }

  def main(args: Array[String]) {
    val seed = System.currentTimeMillis.toInt
    val (puzzle, solved) = createWicked(seed, "classic")
    println(s"$puzzle $solved")
  }
}

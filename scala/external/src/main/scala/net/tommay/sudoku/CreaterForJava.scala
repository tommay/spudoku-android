package net.tommay.sudoku

import scala.util.Random

// Provide a "create" function intended to be called from Java that
// does all the heavy lifting like Random creation and Layout lookup
// in Scala.

// It's not enough to create puzzles with the right Hruristics.  We
// also need to ensure the puzzles can't be solved without using the
// heuristics tht give them their difficulty level.

object CreaterForJava {
  // EasyPeasy puzzles can be solved using only Heuristic.EasyPeasy.
  // 0m38.704s

  @throws(classOf[InterruptedException])
  def createEasyPeasy(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    val createOptions = new SolverOptions(
      List(Heuristic.EasyPeasy), false, false)
    createFiltered(randomSeed, layoutName, createOptions)
  }

  // EasyPeasy and MissingOne are both subsets of Needed, but they
  // are the easiest subsets of Needed to find visually.  The same
  // puzzles will be created no matter what the order is, but put
  // MissingOne first because it's faster.
  // 0m31.961s

  @throws(classOf[InterruptedException])
  def createEasy(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    val createOptions = new SolverOptions(
      List(Heuristic.MissingOne, Heuristic.EasyPeasy), false, false)
    createFiltered(randomSeed, layoutName, createOptions)
  }

  // Medium puzzles require Heuristic.Needed.
  // 0m20.618s

  @throws(classOf[InterruptedException])
  def createMedium(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    val createOptions = new SolverOptions(
      List(Heuristic.Needed, Heuristic.MissingOne, Heuristic.EasyPeasy),
      false, false)
    val solveOptions = new SolverOptions(
      List(Heuristic.MissingOne, Heuristic.EasyPeasy),
      false, false)
    createFiltered(randomSeed, layoutName, createOptions,
      (puzzle, solution) =>
      // Checking for Heuristic.Needed here is a tiny win.
      solution.steps.exists(_.tjpe == Heuristic.Needed) &&
        !solvableWith(puzzle, solveOptions))
  }

  // Vicious puzzles have Forced cells but no Guessing.
  // 2m11.172s

  @throws(classOf[InterruptedException])
  def createVicious(randomSeed: Int, layoutName: String) : (String, String) = {
    val createOptions = new SolverOptions(
      List(Heuristic.Forced, Heuristic.Needed, Heuristic.Tricky), false, false)
    val solveOptions = new SolverOptions(
      List(Heuristic.Needed, Heuristic.Tricky), false, false)
    createFiltered(randomSeed, layoutName, createOptions,
      (puzzle, solution) =>
      // Checking for Heuristic.Forced here is a small win.
      solution.steps.exists(_.tjpe == Heuristic.Forced) &&
        !solvableWith(puzzle, solveOptions))
  }

  // Wicked puzzles require Guessing, even with all our heuristics.
  // 1m6.223s

  @throws(classOf[InterruptedException])
  def createWicked(randomSeed: Int, layoutName: String) : (String, String) = {
    val createOptions = new SolverOptions(
      List(), false, true)
    val solveOptions = new SolverOptions(
      List(Heuristic.Forced, Heuristic.Needed, Heuristic.Tricky), false, false)
    createFiltered(randomSeed, layoutName, createOptions,
      (puzzle, solution) => !solvableWith(puzzle, solveOptions))
  }

  @throws(classOf[InterruptedException])
  def createFiltered(
    randomSeed: Int,
    layoutName: String,
    options: SolverOptions,
    pred: (Puzzle, Solution) => Boolean = (_, _) => true)
      : (String, String) =
  {
    val rnd = new Random(randomSeed)
    val solveFunc = Solver.solutions(options)(_)
    Layout.getLayout(layoutName) match {
      case None => ("", "")
      case Some(layout) =>
        val puzzles = Creater.createStreamWithSolution(rnd, layout, solveFunc)
        val filteredPuzzles = puzzles.filter{case (puzzle, solution) =>
          if (Thread.interrupted) {
            println("Spudoku Interrupted in createFiltered")
            throw new InterruptedException
          }
          pred(puzzle, solution)}  //.drop(1000) // For testing.  XXX!!!
        val (puzzle, solution) = filteredPuzzles.head
        (puzzle.toString, solution.puzzle.toString)
    }
  }

  def solvableWith(puzzle: Puzzle, options: SolverOptions) : Boolean = {
    Solver.solutions(options)(puzzle).nonEmpty
  }

  // CreaterForJava can be run independently for testing.

  def main(args: Array[String]) {
    val seed = 1 // System.currentTimeMillis.toInt
    val (puzzle, solved) = createWicked(seed, args(0))
    println(s"$puzzle $solved")
  }
}

package net.tommay.sudoku

import scala.util.Random

// Provide a "create" function intended to be called from Java that
// does all the heavy lifting like Random creation and Layout lookup
// in Scala.

object CreaterForJava {
  def createEasyPeasy(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    val options = new SolverOptions(
      List(Heuristic.EasyPeasy), false, false)
    createFiltered(randomSeed, layoutName, options)
  }

  def createEasy(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    // EasyPeasy and MissingOne are both subsets of Needed, but they
    // are the easiest subsets of Needed to find visually.  The same
    // puzzles will be created no matter what the order is, but put
    // MissingOne first because it's faster.

    val options = new SolverOptions(
      List(Heuristic.MissingOne, Heuristic.EasyPeasy), false, false)
    createFiltered(randomSeed, layoutName, options)
  }

  // XXX See the note on createVicious about why we need a more
  // complex predicate to get truly medium/vicious puzzles.
  def createMedium(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    val options = new SolverOptions(
      List(Heuristic.Needed, Heuristic.MissingOne, Heuristic.EasyPeasy),
      false, false)
    createFiltered(randomSeed, layoutName, options,
      solution => solution.steps.exists(_.tjpe == Heuristic.Needed))
  }

  // Vicious puzzles have Forced cells but no Guessing.  XXX This
  // doesn't work.  Just because one Solution requires Forced doesn't
  // mean there are Solutions that don't need it.  To make sure, we'd
  // need to try to solve without Forced and if it fails then Forced
  // is required.

  def createVicious(randomSeed: Int, layoutName: String) : (String, String) = {
    val options = new SolverOptions(
      List(Heuristic.Forced, Heuristic.Needed, Heuristic.Tricky), false, false)
    createFiltered(randomSeed, layoutName, options,
      solution => solution.steps.exists(_.tjpe == Heuristic.Forced))
  }

  // Wicked puzzles require Guessing.

  def createWicked(randomSeed: Int, layoutName: String) : (String, String) = {
    val options = new SolverOptions(
      List(Heuristic.Forced, Heuristic.Needed, Heuristic.Tricky), false, true)
    createFiltered(randomSeed, layoutName, options,
      solution => solution.steps.exists(_.tjpe == Heuristic.Guess))
  }

  def createFiltered(
    randomSeed: Int,
    layoutName: String,
    options: SolverOptions,
    pred: Solution => Boolean = (solution => true))
      : (String, String) =
  {
    val rnd = new Random(randomSeed)
    val solveFunc = Solver.solutions(options)(_)
    Layout.getLayout(layoutName) match {
      case None => ("", "")
      case Some(layout) =>
        val puzzles = Creater.createStreamWithSolution(rnd, layout, solveFunc)
        val filteredPuzzles = puzzles.filter{case (_, solution) =>
          pred(solution)}
        val (puzzle, solution) = filteredPuzzles.head
        (puzzle.toString, solution.puzzle.toString)
    }
  }

  // CreaterForJava can be run independently for testing.

  def main(args: Array[String]) {
    val seed = 1 // System.currentTimeMillis.toInt
    val (puzzle, solved) = createMedium(seed, args(0))
    println(s"$puzzle $solved")
  }
}

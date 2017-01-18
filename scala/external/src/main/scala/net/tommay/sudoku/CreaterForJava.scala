package net.tommay.sudoku

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
}

package net.tommay.sudoku

// Provide a "create" function intended to be called from Java that
// does all the heavy lifting like Random creation and Layout lookup
// in Scala.

object CreaterForJava {
  def create(randomSeed: Int, layoutName: String)
      : (String, String) =
  {
    val rnd = new scala.util.Random(randomSeed)
    Layout.getLayout(layoutName) match {
      case None => ("", "")
      case Some(layout) =>
        val (puzzle, solution) = Creater.createWithSolution(
          rnd, layout, Solver.solutions(SolverOptions.all))
        (puzzle.toString, solution.toString)
    }
  }
}

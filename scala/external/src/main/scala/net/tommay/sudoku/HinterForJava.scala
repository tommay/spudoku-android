package net.tommay.sudoku

// Provide a "getHint" function intended to be called from Java
// that does all the heavy lifting like Random creation and dredging
// information out of the Solution in Scala.

object HinterForJava {
  def getHint(randomSeed: Int, puzzleString: String) : Option[Hint] = {
    // Heuristics are listed here from easiest for humans to do, to
    // hardest.
    val options = new SolverOptions(
      List(
        Heuristic.MissingOne,
        Heuristic.EasyPeasy,
        Heuristic.Needed,
        Heuristic.MissingTwo,
        Heuristic.Tricky,
        Heuristic.Forced),
      false, true, false)
    val rnd = new scala.util.Random(randomSeed)
    val puzzle = Puzzle.fromString(puzzleString)
    val solution = Solver.randomSolutions(options, rnd)(puzzle).head
    val stepsWithPlacement = solution.steps.filter(_.placementOption.isDefined)
    stepsWithPlacement match {
      case (step :: _) =>
        Some(Hint(step.tjpe, step.placementOption.get, step.cells))
      case _ => None
    }
  }

  // Load a puzzle from the given file and print its randomized hint.

  def main(args: Array[String]) {
    val filename = args(0)
    val puzzleString = Solve.getSetup(filename)
    val seed = System.currentTimeMillis.toInt
    println(getHint(seed, puzzleString))
  }
}

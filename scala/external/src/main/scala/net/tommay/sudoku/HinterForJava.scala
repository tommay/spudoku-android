package net.tommay.sudoku

// Provide a "getHintEasy" function intended to be called from Java
// that does all the heavy lifting like Random creation and dredging
// information out of the Solution in Scala.

object HinterForJava {
  def getHintEasy(randomSeed: Int, puzzleString: String) : String = {
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
    getHint(randomSeed, puzzleString, options)
  }

  def getHint(randomSeed: Int, puzzleString: String, options: SolverOptions)
      : String =
  {
    val rnd = new scala.util.Random(randomSeed)
    val puzzle = Puzzle.fromString(puzzleString)
    val solution = Solver.randomSolutions(options, rnd)(puzzle).head
    val stepsWithPlacement = solution.steps.filter(_.placementOption.isDefined)
    stepsWithPlacement match {
      case (step :: _) => makeHintString(step)
      case _ => "Solved!"
    }
  }

  def makeHintString(step: Step) : String = {
    if (step.tjpe == Heuristic.EasyPeasy) {
      val placement = step.placementOption.get
      s"Easy peasy ${placement.digit} ${placement}"
    }
    else {
      val placement = step.placementOption.get
      s"${step.description} ${placement}" // XXX
    }
  }

  // Load a puzzle from the given file and print its randomized hint.

  def main(args: Array[String]) {
    val filename = args(0)
    val puzzleString = Solve.getSetup(filename)
    val seed = System.currentTimeMillis.toInt
    println(getHintEasy(seed, puzzleString))
  }
}

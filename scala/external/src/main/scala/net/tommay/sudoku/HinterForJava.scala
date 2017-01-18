package net.tommay.sudoku

// Provide a "getHintEasy" function intended to be called from Java
// that does all the heavy lifting like Random creation and dredging
// information out of the Solution in Scala.

object HinterForJava {
  def getHintEasy(randomSeed: Int, puzzleString: String) : String = {
    // XXX EasyPeasy and MissingOne are both subsets of Needed, but they
    // are the easiest subsets of Needed to find visually.
    val options = new SolverOptions(
      List(Heuristic.EasyPeasy, Heuristic.MissingOne), false, false)
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
      step.description // XXX
    }
  }
}

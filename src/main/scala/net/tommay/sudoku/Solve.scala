package net.tommay.sudoku

import scala.util.Random

object Solve {
  // Initializes Puzzle from the given Filename and prints out solutions
  // if any.

  def main(args: Array[String]) {
    val filename = args(0)
    val setup = getSetup(filename)
    val puzzle = Puzzle.fromString(setup)
    val rnd = new Random(2)
    // It is critical not to put the result of randomSolutions in a
    // val here because that holds onto the head even though it would
    // just be passed to processAndCount and the val would become dead
    // and collectable.  Scala/JVM doesn't see it that way.
    // We get away with passing it to processAndCount which is recursive
    // so the top-level call has the head, except that Scala properly
    // optimizes the recursive tail call so we're ok.
    val count = processAndCount(
      Solver.randomSolutions(options, rnd)(puzzle),
      printSolution)
    println(s"There are $count solutions.")
  }

  val heuristics = List(Heuristic.Needed, Heuristic.Forced)

  val options = new SolverOptions(
    heuristics = heuristics,
    usePermanentTrickySets = false,
    useGuessing = true)

  def processAndCount[T](list: Iterable[T], func: T => Unit, count: Int = 0)
    : Int =
  {
    list.headOption match {
      case Some(head) =>
        func(head)
        processAndCount(list.tail, func, count + 1)
      case _ => count
    }
  }

  def printSolution(solution: Solution) {
    if (false) {
      solution.steps.foreach {step =>
        println(showStep(step))
      }
      println(solution.puzzle.toPuzzleString)
    }
  }

  def showStep(step: Step) : String = {
    step.tjpe.toString + (step.placementOption match {
      case Some(placement) =>
        val (row, col) = rowcol(placement.cellNumber)
        s": ($row, $col) ${placement.digit}"
      case None => ""
    })
  }

  def rowcol(n: Int) = {
    (n / 9, n % 9)
  }

  // Returns the contents of filename as a string with "#" comments
  // and whitespace deleted.  The result should be a string of 81
  // digits or dashes, where the digits are given by the puzzle and
  // the dash cells are to be solved for.

  def getSetup(filename: String) = {
    val raw = scala.io.Source.fromFile(filename).mkString
    val noComments = "#.*".r.replaceAllIn(raw, "")
    val setup = """\s+""".r.replaceAllIn(noComments, "")
    setup
  }
}

package net.tommay.sudoku

import scala.util.Random

case class Solver (
  options: SolverOptions,
  rnd: Option[Random],
  puzzle: Puzzle,
  unknowns: Stream[Unknown],
  // steps is consed in reverse order.  It is reversed when
  // constructing a Solution.
  steps: List[Step],
  heuristics: Stream[Solver => Stream[Next]])
{
  def place(cellNumber: Int, digit: Int) : Solver = {
    val newPuzzle = puzzle.place(cellNumber, digit)
    val newUnknowns = unknowns
      .filter(_.cellNumber != cellNumber)
      .map(_.place(cellNumber, digit))
    this.copy(puzzle = newPuzzle, unknowns = newUnknowns)
  }

  // All of the calls in the solutions chain which eventually may come
  // back around to top are tail calls, but scala doesn't do tail call
  // optimization except for direct recursion.  But we should make
  // at most 81 deep nested calls to solutionsTop to solve a puzzle so
  // we shouldn't blow the stack.  If we do, there are always trampolines:
  // http://stackoverflow.com/questions/16539488/why-scala-doesnt-make-tail-call-optimization
  // scala.util.control.TailCalls
  // 
  def solutionsTop : Stream[Solution] = {
    unknowns match {
      case Stream.Empty  =>
        // No more unknowns, solved!
        Stream(Solution(puzzle, steps.reverse))
      case _ =>
        // Carry on with solutionsHeuristic
        solutionsHeuristic
    }
  }

  def solutionsHeuristic : Stream[Solution] = {
    if (options.useHeuristics) {
      val (rnd1, rnd2) = Solver.maybeSplit(rnd)
      heuristics.flatMap {func =>
        func(this) match {
          case empty@Stream.Empty => empty
          case stream =>
            rnd1 match {
              case None => stream
              // We're only going to be using the head of the whole
              // heuristics Stream (see below), so just keep one random
              // element of the heuristic's results, if any, in the Stream.
              case Some(rnd1) => Stream(Solver.pickRandom(rnd1, stream))
            }
        }
      } match {
        case Stream.Empty =>
          // All heuristics returned empty lists.
          solutionsStuck
        case nextList =>
          val nextSolver = this.copy(rnd = rnd2)
          val next = nextList.head
          nextSolver.placeAndContinue(next)
      }
    }
    else {
      // Skip the heuristics and continue with solutionsStuck.
      solutionsStuck
    }
  }

  def placeAndContinue(next: Next) : Stream[Solution] = {
    val placement = next.placement
    val newSolver = place(placement.cellNumber, placement.digit)
    val step = Step(
      newSolver.puzzle, Some(placement), next.tjpe, next.description)
    val newSteps = step :: steps
    val newSolver2 = newSolver.copy(steps = newSteps)
    newSolver2.solutionsTop
  }

  def solutionsStuck : Stream[Solution] = {
    // We get here because we can't place a digit using human-style
    // heuristics, so we've either failed or we have to guess and
    // recurse.  We can distinguish by examining the cell with the
    // fewest possibilities remaining, which is also the best cell to
    // make a guess for.

    // I tried using unknowns.minBy but ig was slower, wtf.
    val minUnknown = Util.minBy(unknowns, {x: Unknown => x.numPossible})
    val cellNumber = minUnknown.cellNumber
    // I tried matching on minUnknown.getPossible and only binding
    // possible in case _, but it was slower, wtf.
    val possible = minUnknown.getPossible
    possible match {
      case Nil =>
        // Failed.  No solutions.
        Stream.empty
      case List(digit) =>
        // One possibility.  The choice is forced, no guessing.  But
        // we only use the force if a) we're guessing, and b) we're
        // not using heuristics, because if we are then forcing is
        // done by Heuristic.Forced via findForced.
        if (options.useGuessing && !options.useHeuristics) {
          val next = Next(Heuristic.ForcedGuess, "Forced guess",
            Placement(cellNumber, digit))
          placeAndContinue(next)
        }
        else {
          // There is a forced guess but we're not configured to use
          // it.  See if we can apply a TrickySet to create an
          // opportunity.
          applyOneTrickySetIfAllowed match {
            case Some(newSolver) => newSolver.solutionsTop
            case _ => Stream.empty
          }
        }
      case  _ =>
        // Multiple possibilities.  Before we guess, see if it's
        // possible to permanently apply a TrickySet to create
        // possibiities for heuristics.
        applyOneTrickySetIfAllowed match {
          case Some(newSolver) => newSolver.solutionsTop
          case _ =>
            if (options.useGuessing) {
              // Guess each possibility, maybe in a random order, and
              // recurse.  We could use Random.split when shuffling or
              // recursing, but it's not really important for this
              // application.
              val shuffledPossible = Solver.maybeShuffle(rnd, possible)
              doGuesses(cellNumber, shuffledPossible)
            }
            else {
              Stream.empty
            }
        }
    }
  }

  // For each digit in the list, use it as a guess for unknown
  // and try to solve the resulting Puzzle.

  def doGuesses(cellNumber: Int, digits: Iterable[Int])
    : Stream[Solution] =
  {
    digits.foldLeft(Stream.empty[Solution]) {(accum, digit) =>
      val next = Next(Heuristic.Guess, "Guess", Placement(cellNumber, digit))
      accum #::: placeAndContinue(next)
    }
  }

  // Try to place a digit where an ExclusionSet has only one unplaced
  // cell.

  def findMissingOne : Stream[Next] = {
    ExclusionSet.exclusionSets.flatMap{findMissingOneInSet(_)}
  }

  def findMissingOneInSet(set: ExclusionSet) : Stream[Next] = {
    Solver.unknownsInSet(unknowns, set.cells) match {
      case Stream(unknown) =>
        // Exactly one cell in the set is unknown.  Place a digit in
        // it.  Note that since this is the only unknown position in
        // the set there should be exactly one possible digit
        // remaining.  But we may have made a wrong guess, which
        // leaves no possibilities.
        findForcedForUnknown(
          Heuristic.MissingOne, s"Missing one in ${set.name}")(unknown)
      case _ =>
        // Zero or multiple cells in the set are unknown.
        Stream.Empty
    }
  }

  // Try to place a digit where a set has two unplaced cells.  We only
  // place one of the digits but the second will follow quickly.

  def findMissingTwo : Stream[Next] = {
    ExclusionSet.exclusionSets.flatMap(findMissingTwoInSet(_))
  }

  def findMissingTwoInSet(set: ExclusionSet) : Stream[Next] = {
    Solver.unknownsInSet(unknowns, set.cells) match {
      case unknowns@Stream(_, _) =>
        // Exactly two cells in the set are unknown.  Place digits in
        // them if they are forced.  A random one will be chosen
        // upstream if necessary (and if we find anything to return).
        unknowns.flatMap(findForcedForUnknown(
          Heuristic.MissingTwo, s"Missing two in ${set.name}"))
      case _ =>
        // Zero or too many unknowns for humans to easiy handle.
        Stream.Empty
    }
  }

  // Try to place a digit where there is a set that doesn't yet have
  // some digit (i.e., it needs it) and there is only one cell in the
  // set where it can possibly go.

  def findNeeded : Stream[Next] = {
     ExclusionSet.exclusionSets.flatMap(findNeededInSet(_))
  }

  def findNeededInSet(set: ExclusionSet) : Stream[Next] = {
    Solver._1to9.flatMap(Solver.findNeededDigitInSet(
      unknowns, set, Heuristic.Needed, s"Needed in ${set.name}"))
  }

  def findForced : Stream[Next] = {
    // Currying is somewhat ugly in scala, but seems to be a smidge
    // faster,
    unknowns.flatMap(findForcedForUnknown(Heuristic.Forced, "Forced"))
  }

  // This can return either List or Stream.  But since it's going to be
  // flatMap'd by a Stream, returning Stream performs better.

  def findForcedForUnknown
    (tjpe: Heuristic.Value, description: => String)
    (unknown: Unknown) :
    Stream[Next] =
  {
    unknown.numPossible match {
      case 1 =>
        val digit = unknown.getPossible.head
        Stream(Next(tjpe, description, Placement(unknown.cellNumber, digit)))
      case _ =>
        Stream.empty
    }
  }

  def applyOneTrickySetIfAllowed : Option[Solver] = {
    if (options.usePermanentTrickySets) {
      None // XXX
    }
    else {
      None
    }
  }

  def findEasyPeasy : Stream[Next] = {
    EasyPeasy.find(puzzle, unknowns)
  }

  def findTricky : Stream[Next] = {
    Stream.empty
  }
}

object Solver {
  // Having this pre-constructed makes a significant difference over
  // calling toStream each time it's needed.

  val _1to9 = (1 to 9).toStream

  def create(options: SolverOptions, rnd: Option[Random], puzzle: Puzzle)
    : Solver =
  {
    val (rnd1, rnd2) = maybeSplit(rnd)
    val unknowns = maybeShuffle(rnd1, (0 to 80).map(Unknown(_))).toStream
    val step = Step(puzzle, None, Heuristic.Initial, "Initial puzzle")
    val heuristicFunctions =
      options.heuristics.map(getHeuristicFunction).toStream
    val solver = new Solver(
      options, rnd, puzzle, unknowns, List(step), heuristicFunctions)
    puzzle.each.foldLeft(solver) {case (accum, (cellNumber, digit)) =>
      accum.place(cellNumber, digit)
    }
  }

  // Heuristic functions return a Stream.  We may need only the first
  // result (when creating a Puzzle, to see whether the Puzzle is
  // solvable), or we may pick a random result (when providing a
  // hint).  Using a Stream makes it ok either way since we'll only
  // compute what we need.

  def getHeuristicFunction(heuristic: Heuristic.Value)
      : Solver => Stream[Next] = {
    heuristic match {
      case Heuristic.EasyPeasy => {_.findEasyPeasy}
      case Heuristic.MissingOne => {_.findMissingOne}
      case Heuristic.MissingTwo => {_.findMissingTwo}
      case Heuristic.Tricky => {_.findTricky}
      case Heuristic.Needed => {_.findNeeded}
      case Heuristic.Forced => {_.findForced}
    }
  }

  // Try to solve the Puzzle, returning a list of Solutions.

  def solutions(options: SolverOptions)(puzzle: Puzzle) : Stream[Solution] = {
    val solver = Solver.create(options, None, puzzle)
    solver.solutionsTop
  }

  def randomSolutions
    (options: SolverOptions, rnd: Random)
    (puzzle: Puzzle)
      : Stream[Solution] =
  {
    val solver = Solver.create(options, Some(rnd), puzzle)
    solver.solutionsTop
  }

  // Compute all the solutions and return them in a random order.

  def allRandomSolutions(rnd: Random)(puzzle: Puzzle) = {
    randomSolutions(SolverOptions.all, rnd)(puzzle)
  }

  def maybeSplit(rnd: Option[Random]) : (Option[Random], Option[Random]) = {
    rnd match {
      case Some(rnd) =>
        val (rnd1, rnd2) = Util.split(rnd)
        (Some(rnd), Some(rnd))
      case _ => (None, None)
    }
  }

  // XXX Maybe a set intersection?  Use a Set for the larger set?

  def unknownsInSet(unknowns: Stream[Unknown], set: Set[Int])
    : Stream[Unknown] =
  {
    unknowns.filter(u => set.contains(u.cellNumber))
  }

  def findNeededDigitInSet
    (unknowns: Stream[Unknown], exclusionSet: ExclusionSet,
      tjpe: Heuristic.Value, description: => String)
    (digit: Int)
    : Stream[Next] =
  {
    val unknownsFromSet = unknownsInSet(unknowns, exclusionSet.cells)
    unknownsFromSet.filter(_.isDigitPossible(digit)) match {
      case Stream(unknown) =>
        Stream(Next(tjpe, description, Placement(unknown.cellNumber, digit)))
      case _ => Stream.empty
    }
  }

  def maybeShuffle[T](rnd: Option[Random], list: Iterable[T]) : Iterable[T] = {
    rnd match {
      case Some(rnd) => Util.shuffle(list.toList, rnd)
      case _ => list
    }
  }

  import scala.reflect.ClassTag

  def pickRandom[T:ClassTag](rnd: Random, list: Iterable[T]) : T = {
    // Forget being tricky and general and handling arbitrarily
    // large Iterables and Streaming shuffled results.  Just
    // materialize the thing and pick something.
    val array = list.toArray
    array(rnd.nextInt(array.size))
  }
}

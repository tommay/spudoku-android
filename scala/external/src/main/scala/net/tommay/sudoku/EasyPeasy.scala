package net.tommay.sudoku

// Given a Stripe pf three columns (or rows) col0, col1, and col2.
// Count the occurences of each digit in the entire Stripe.  For each
// digit with two occurences, check col0/col1/col2 to see if there's
// only one Unknown where the digit is possible.

// List performs better than Vector for cells.

case class Stripe(
  cells: List[Int],
  exclusionSets: Stream[ExclusionSet])

object EasyPeasy {
  // Easy peasies are found by using stripes of three rows or columns.
  // The first item in the tuple is the set we're looking to place a
  // digit in.  The Iterable has the other two rows/columns in the
  // stripe.  Here we build all the possible stripes so they can be
  // searched for easy peasies.

  val stripes : Stream[Stripe] = {
    Util.slices(3, (ExclusionSet.rows ++ ExclusionSet.columns))
      .toStream
      .map(_.toStream)
      .map(makeStripe)
  }

  def makeStripe(exclusionSets: Stream[ExclusionSet]) : Stripe = {
    val allCells = exclusionSets.foldLeft(Set.empty[Int]){
      case (accum, exclusionSet) => accum ++ exclusionSet.cells
    }.toList
    Stripe(allCells, exclusionSets)
  }

  // Return a Stream of all possible easy peasy placements for the Puzzle.

  def find(puzzle: Puzzle, unknowns: Stream[Unknown]) : Stream[Next] = {
    stripes.flatMap(findForEasyPeasyStripe(puzzle, unknowns))
  }

  // Returns any easy peasies in the Puzzle and Stripe.  All digits
  // are considered

  def findForEasyPeasyStripe
    (puzzle: Puzzle, unknowns: Stream[Unknown])
    (stripe: Stripe)
    : Stream[Next] =
  {
    val placed = puzzle.placed
    val allDigits =
      stripe.cells.foldLeft(List.empty[Int]) {case (accum, cellNumber) =>
        placed.get(cellNumber) match {
          case None => accum
          case Some(digit) => digit :: accum
        }
      }
    val doubleDigits = allDigits
      .groupBy(identity)
      .toStream
      .withFilter{case (_, list) => list.size == 2}
      .map{case (digit, _) => digit}
    stripe.exclusionSets.flatMap(blah(unknowns, doubleDigits))
  }

  def blah
    (unknowns: Stream[Unknown], digits: Stream[Int])
    (exclusionSet: ExclusionSet)
      : Stream[Next] =
  {
    digits.flatMap(Solver.findNeededDigitInSet(
      unknowns, exclusionSet.cells, Heuristic.EasyPeasy))
  }
}

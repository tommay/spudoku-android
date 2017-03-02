package net.tommay.sudoku

// Each Stripe has three ExclusionSet rows or columns.  It has a Set
// of all column numbers in the Stripe, and a Stream of the three
// ExclusionSets.  To find an EasyPeasy, count the occurences of each
// digit in the entire Stripe.  For each digit with two occurences,
// check for an ExclusionSet with only one Unknown where the digit is
// possible.

// List performs better than Vector for cells.

case class Stripe(
  cells: List[Int],
  exclusionSets: Stream[ExclusionSet])

object EasyPeasy {
  // Build all the possible Stripes so they can be searched for easy
  // peasies.

  val stripes : Stream[Stripe] = {
    Util.slices(3, (ExclusionSet.rows ++ ExclusionSet.columns))
      .toStream
      .map(_.toStream)
      .map(makeStripe)
  }

  // Take a stripe of three ExclusionSets in a Stream and create a
  // Stripe with rhe combined Set of all the cell numbers, and the
  // ExclusionSets.
  //
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
    // This foldLeft is faster than the straightforward
    // flatMap(cellNumber => placed.get(cellNumber)), ridiculous.
    // And it's much fasterto generate allDigits and do the groupBy than it
    // is to tally the counts into a Map to begin with.
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

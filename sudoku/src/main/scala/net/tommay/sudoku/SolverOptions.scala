package net.tommay.sudoku

import net.tommay.sudoku.Heuristic._

// EasyPeasy: An easy pattern to spot visually. where two rows or columns
//   in a 3-stripe contain a digit and there is only one place in the
//   remaining column where it can go.  This is a subset of Needed, but is
//   easy to spot.
// MissingOne: A set is missing only one digit.  This is a subset of
//   both Needed and Forced, but is easier to spot.
// MissingTwo: A set is missing two digits, and at least one is Forced.  The
//   remaining digit will eventually be found by MissongOne or some other
//   means.
// Tricky: A square is missing a digit and there is only one row or column
//   where it can go.  There is not enough information to place the digit
//   in the square, but digit can be reliminated as possibility for the
//   rest of the row or column.  For the heuristic we just check whether
//   the containing squares need the digit and there is now just one place
//   for it in the square.
// Needed: A set doesn't have a digit and there is only one place it can go.
// Forced: A cell has only one possibility, all others having been eliminated.
//   This is most tedious to spot.
//
// Of the heurisrics, only Needed, Tricky, and Forced are capable of
// solving some puzzles when used on their own, and none is a subset
// of any other nor the combination of the others.
//
// Whereas the Tricky heuristic removes possibilities only while checking
// for needed digits, the usePermanentTrickySets option will eliminate
// the possibilities for the rest of the solution steps, and will also
// eliminate using "inverse" TrickySets in which possibilities are removed
// from the remainder of a square intead of a row/column.  The idea is to
// get the strongest solver that doesn't require guessing, even though
// it's impossible to solve these puzzles visually.

case class SolverOptions(
  useHeuristics: Boolean,
  heuristics: Iterable[Heuristic],
  usePermanentTrickySets: Boolean,
  useGuessing: Boolean)
{
  def this(heuristics: Iterable[Heuristic],
           usePermanentTrickySets: Boolean,
           useGuessing: Boolean)
  {
    this(heuristics.nonEmpty, heuristics, usePermanentTrickySets, useGuessing)
  }
}

object SolverOptions {
  val all = new SolverOptions(List(), false, true)

  // Try Forced first because it's fast.  MissingOne and MissingTwo
  // are redundant with Forced.  EasyPeasy is redundant with Needed.

  val noGuessing = new SolverOptions(
    List(Forced, Needed, Tricky), false, false)
}

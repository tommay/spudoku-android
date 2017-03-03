package net.tommay.sudoku

case class TrickySet (
  name: String,
  common: Stream[Int],  // XXX Set?
  rest: Stream[Int],    // XXX Set?
  eliminate: Stream[Int],
  checkNeeded: Stream[Set[Int]])

// True to check an "inverted" TrickySet, where we scan the coincident
// rows and columns for needed digits rather than coincident squares.
// I personally don't do this, it's too hard.
//
// XXX checkCoincidentRowsColumnsForNeeded = False

// XXX Checking the eliminate set for newly forced Unknowns isn't implemented.
// It's also not something I do.

// Within a square, if the only possible places for a given digit are
// in the same row/col, then the digit can be removed from the
// possibilities for the rest of the Unknowns in that row/col.
//
// The reverse of the situation is also true.  In a given row or
// column if it is only possible to place a given digit within a
// single square, then the digit can be eliminated from the other
// Unknowns of that square.
//
// Each tuple in trickySets contains three lists of Unknowns.  If a
// digit is possible in the first list but not the second, it will be
// removed from the possibiles of the third.
//

// After we apply a tricky set with a particular digit, it may create
// some immediate placements:
// - There may be 

// - Some of the Unknowns in the eliminate set may now have only one possibility.

object TrickySet {
  val trickySets: Stream[TrickySet] = {
    createTrickySets(false)
  }

  val inverseTrickySets: Stream[TrickySet] = {
    createTrickySets(true)
  }

  val allTrickySets = {
    trickySets ++ inverseTrickySets
  }

  def createTrickySets(inverse: Boolean) : Stream[TrickySet] = {
    val rows = ExclusionSet.rows.toStream
    val columns = ExclusionSet.columns.toStream
    val squares = ExclusionSet.squares.toStream
    val getRows = getCellSetsIncluding(rows)(_)
    val getColumns = getCellSetsIncluding(columns)(_)
    val set1 = for (square <- squares; row <- rows) yield
      createTrickySetsFrom(inverse, square, row, getColumns)
    val set2 = for (square <- squares; col <- columns) yield
      createTrickySetsFrom(inverse, square, col, getRows)
    (set1 ++ set2).flatten
  }

  def createTrickySetsFrom(
    inverse: Boolean,
    square: ExclusionSet,
    row: ExclusionSet,
    getSetsIncluding: Set[Int] => Stream[Set[Int]])
      : Option[TrickySet] =
  {
    val common = square.cells.intersect(row.cells)
    if (common.isEmpty) {
      None
    }
    else {
      val restOfRow = row.cells.diff(common)
      val restOfSquare = square.cells.diff(common)
      Some(
        if (!inverse) {
          TrickySet(
            name = s"TrickySet ${square.name} ${row.name}",
            common = common.toStream,
            rest = restOfSquare.toStream,
            eliminate = restOfRow.toStream,
            checkNeeded = getSquaresIncluding(restOfRow)
              .map(_ -- restOfRow))
        }
        else {
          TrickySet(
            name = s"Inverse TrickySet ${square.name} ${row.name}",
            common = common.toStream,
            rest = restOfRow.toStream,
            eliminate = restOfSquare.toStream,
            checkNeeded = getSetsIncluding(restOfSquare)
              .map(_ -- restOfSquare))
        }
      )
    }
  }

  // Given some ExclusionSets and some cellNumbers, return the
  // ExclusionSets that contain them.
  //
  def getCellSetsIncluding
    (exclusionSets: Stream[ExclusionSet])
    (cells: Set[Int])
      : Stream[Set[Int]] =
  {
    val cellSets = exclusionSets.map(_.cells)
    cellSets.filter(_.intersect(cells).nonEmpty)
  }

  // Given some cellNumbers, return the ExclusionSet squares containing
  // them.
  //
  def getSquaresIncluding(cells: Set[Int]) : Stream[Set[Int]] = {
    getCellSetsIncluding(ExclusionSet.squares.toStream)(cells)
  }
}

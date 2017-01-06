package net.tommay.sudoku

// An ExclusionSet is a named List of all the cell numbers in a row,
// column, or square.  They're used as part of the heuristic methods
// in Solver.

// XXX Everything ends up being Vectors.  Compare speed to List.

case class ExclusionSet(
  name: String,
  cells: Set[Int])

object ExclusionSet {
  // An ExclusionSet for each row.

  val rows : Iterable[ExclusionSet] =
    (0 to 8).map(row)

  def row(n: Int) : ExclusionSet = {
    ExclusionSet(s"row $n", (0 to 8).map(col => n*9 + col).toSet)
  }

  // An ExclusionSet for each column.

  val columns : Iterable[ExclusionSet] =
    (0 to 8).map(column)

  def column(n: Int) : ExclusionSet = {
    ExclusionSet(s"column $n", (0 to 8).map(row => row*9 + n).toSet)
  }

  // An ExclusionSet for each square.

  val squares : Iterable[ExclusionSet] =
    (0 to 8).map(square)

  def square(n: Int) : ExclusionSet = {
    // row and col of upper left corner of square
    val row = n / 3 * 3
    val col = n % 3 * 3
    val cellNumbers = (0 to 8).map(n => (row + n / 3)*9 + (col + n % 3)).toSet
    ExclusionSet(s"square $n", cellNumbers)
  }

  // All ExclusionSets.

  val exclusionSets : Stream[ExclusionSet] =
    (rows ++ columns ++ squares).toStream
}



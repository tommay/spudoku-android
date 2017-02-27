package net.tommay.sudoku

case class Hint(
  tjpe: Heuristic.Value,
  placement: Placement,
  cells: Iterable[Int])

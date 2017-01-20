package net.tommay.sudoku

case class Step(
  puzzle: Puzzle,
  tjpe: Heuristic.Value,
  placementOption: Option[Placement] = None,
  cells: Iterable[Int] = List.empty)

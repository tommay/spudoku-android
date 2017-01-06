package net.tommay.sudoku

case class Step(
  puzzle: Puzzle,
  placementOption: Option[Placement],
  tjpe: Heuristic.Value,
  description: String)

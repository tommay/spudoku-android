package net.tommay.sudoku

case class Step(
  puzzle: Puzzle,
  placementOption: Option[Placement],
  description: String)

package net.tommay.sudoku

// XXX grok Enumeration.

object Heuristic extends Enumeration
{
  type Heuristic = Value
  val
    EasyPeasy,
    MissingOne,
    MissingTwo,
    Needed,
    Forced,
    Tricky
  = Value
}

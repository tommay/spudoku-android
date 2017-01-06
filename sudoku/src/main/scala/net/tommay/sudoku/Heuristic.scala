package net.tommay.sudoku

// XXX grok Enumeration.

object Heuristic extends Enumeration
{
  val
    EasyPeasy,
    MissingOne,
    MissingTwo,
    Needed,
    Forced,
    Tricky  = Value
}

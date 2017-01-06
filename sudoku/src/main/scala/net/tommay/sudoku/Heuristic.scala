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
    Tricky,
    // These are not heuristics, they're just used to type Next and
    // Step.
    Initial,
    Guess,
    ForcedGuess
  = Value
}

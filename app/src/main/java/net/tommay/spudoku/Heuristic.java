package net.tommay.spudoku;

enum Heuristic {
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
}

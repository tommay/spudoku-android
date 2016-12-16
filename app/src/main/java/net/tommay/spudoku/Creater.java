package net.tommay.spudoku;

/** Get a puzzle from the frege code. */

import net.tommay.spudoku.RawPuzzle;
import net.tommay.sudoku.CreaterForJava;

import frege.prelude.PreludeBase;
import frege.prelude.PreludeBase.TTuple2;
import frege.run7.Thunk;

class Creater {
    public static RawPuzzle create(int seed, String layoutName) {
        TTuple2<String,String> t = CreaterForJava.create(
            Thunk.<Integer>lazy(seed), layoutName);
        String puzzle = PreludeBase.<String, String>fst(t);
        String solution = PreludeBase.<String, String>snd(t);
        return new RawPuzzle(puzzle, solution);
    }
}

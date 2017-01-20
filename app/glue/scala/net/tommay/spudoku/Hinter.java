package net.tommay.spudoku;

import scala.Option;
import scala.collection.JavaConverters;

import net.tommay.sudoku.HinterForJava;

import net.tommay.spudoku.Heuristic;
import net.tommay.spudoku.Hint;

class Hinter {
    public static Hint getHint(int randomSeed, String puzzleString) {
        Option<net.tommay.sudoku.Hint> hintOption =
            HinterForJava.getHint(randomSeed, puzzleString);
        if (hintOption.isDefined()) {
            net.tommay.sudoku.Hint hint = hintOption.get();
            return new Hint(
                toHeuristic(hint.tjpe()),
                hint.placement().cellNumber(),
                hint.placement().digit(),
                asJava(hint.cells()));
        }
        else {
            return null;
        }
    }

    private static Heuristic toHeuristic(scala.Enumeration.Value heuristic) {
        return Heuristic.valueOf(heuristic.toString());
    }

    // If it weren't for the :javap command in the Scala REPL I never
    // would have figured this out.  This is what happens under the
    // hood when doing "iter.asJava" in Scala.

    private static Iterable<Integer> asJava(scala.collection.Iterable set) {
        return (Iterable<Integer>)
            JavaConverters.asJavaIterableConverter(set).asJava();
    }
}

package net.tommay.spudoku;

import net.tommay.spudoku.RawPuzzle;
import net.tommay.sudoku.CreaterForJava;

class Creater {
    public static RawPuzzle create (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.create(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }
}

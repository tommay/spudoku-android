package net.tommay.spudoku;

import net.tommay.spudoku.RawPuzzle;
import net.tommay.sudoku.CreaterForJava;

class Creater {
    public static RawPuzzle createEasy (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.createEasy(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }

    public static RawPuzzle createWicked (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.createWicked(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }
}

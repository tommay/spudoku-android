package net.tommay.spudoku;

import net.tommay.spudoku.RawPuzzle;
import net.tommay.sudoku.CreaterForJava;

class Creater {
    public static RawPuzzle createEasyPeasy (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.createEasyPeasy(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }

    public static RawPuzzle createEasy (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.createEasy(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }

    public static RawPuzzle createMedium (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.createMedium(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }

    public static RawPuzzle createVicious (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.createVicious(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }

    public static RawPuzzle createWicked (int seed, String layoutName) {
        scala.Tuple2 result = CreaterForJava.createWicked(
            seed, layoutName);
        return new RawPuzzle((String)result._1, (String)result._2);
    }
}

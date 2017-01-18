package net.tommay.spudoku;

import net.tommay.sudoku.HinterForJava;

class Hinter {
    public static String getHintEasy(int randomSeed, String puzzleString) {
        return HinterForJava.getHintEasy(randomSeed, puzzleString);
    }
}

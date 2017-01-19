package net.tommay.spudoku;

import net.tommay.spudoku.Creater;

class ViciousCreater implements PuzzleCreater {
    @Override
    public RawPuzzle create(int seed, String layoutName) {
        return Creater.createVicious(seed, layoutName);
    }
}

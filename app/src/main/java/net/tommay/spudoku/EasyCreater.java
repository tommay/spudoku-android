package net.tommay.spudoku;

import net.tommay.spudoku.Creater;

class EasyCreater implements PuzzleCreater {
    @Override
    public RawPuzzle create(int seed, String layoutName) {
        return Creater.createEasy(seed, layoutName);
    }
}

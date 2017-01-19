package net.tommay.spudoku;

import net.tommay.spudoku.Creater;

class WickedCreater implements PuzzleCreater {
    @Override
    public RawPuzzle create(int seed, String layoutName) {
        return Creater.createWicked(seed, layoutName);
    }
}

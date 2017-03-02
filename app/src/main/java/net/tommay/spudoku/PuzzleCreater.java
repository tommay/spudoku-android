package net.tommay.spudoku;

import net.tommay.spudoku.Creater;

interface PuzzleCreater {
    public RawPuzzle create(int seed, String layoutName);

    public class Easy implements PuzzleCreater {
        @Override
        public RawPuzzle create(int seed, String layoutName) {
            return Creater.createEasy(seed, layoutName);
        }
    }

    public class Vicious implements PuzzleCreater {
        @Override
        public RawPuzzle create(int seed, String layoutName) {
            return Creater.createVicious(seed, layoutName);
        }
    }

    public class Wicked implements PuzzleCreater {
        @Override
        public RawPuzzle create(int seed, String layoutName) {
            return Creater.createWicked(seed, layoutName);
        }
    }
}

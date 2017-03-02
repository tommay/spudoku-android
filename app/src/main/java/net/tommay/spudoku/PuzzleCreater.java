package net.tommay.spudoku;

import net.tommay.spudoku.Creater;

enum PuzzleCreater {
    EasyPeasy("EasyPeasy", new IPuzzleCreater.EasyPeasy()),
    Easy("Easy", new IPuzzleCreater.Easy()),
    Vicious("Vicious", new IPuzzleCreater.Vicious()),
    Wicked("Wicked", new IPuzzleCreater.Wicked());

    final String name;
    private final IPuzzleCreater puzzleCreater;

    PuzzleCreater (String name, IPuzzleCreater puzzleCreater) {
        this.name = name;
        this.puzzleCreater = puzzleCreater;
    }

    public RawPuzzle create(int seed, String layoutName) {
        return puzzleCreater.create(seed, layoutName);
    }

    private interface IPuzzleCreater {
        public RawPuzzle create(int seed, String layoutName);

        public class EasyPeasy implements IPuzzleCreater {
            @Override
            public RawPuzzle create(int seed, String layoutName) {
                return Creater.createEasyPeasy(seed, layoutName);
            }
        }

        public class Easy implements IPuzzleCreater {
            @Override
            public RawPuzzle create(int seed, String layoutName) {
                return Creater.createEasy(seed, layoutName);
            }
        }

        public class Vicious implements IPuzzleCreater {
            @Override
            public RawPuzzle create(int seed, String layoutName) {
                return Creater.createVicious(seed, layoutName);
            }
        }

        public class Wicked implements IPuzzleCreater {
            @Override
            public RawPuzzle create(int seed, String layoutName) {
                return Creater.createWicked(seed, layoutName);
            }
        }
    }
}

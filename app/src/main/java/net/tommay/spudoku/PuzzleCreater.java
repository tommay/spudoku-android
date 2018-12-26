package net.tommay.spudoku;

import net.tommay.spudoku.Creater;

enum PuzzleCreater {
    EasyPeasy("EasyPeasy", (int seed, String layoutName) ->
        { return Creater.createEasyPeasy(seed, layoutName); }),
    Easy("Easy", (int seed, String layoutName) ->
        { return Creater.createEasy(seed, layoutName); }),
    Medium("Medium", (int seed, String layoutName) ->
        { return Creater.createMedium(seed, layoutName); }),
    Tricky("Tricky", (int seed, String layoutName) ->
        { return Creater.createTricky(seed, layoutName); }),
    Vicious("Vicious", (int seed, String layoutName) ->
        { return Creater.createVicious(seed, layoutName); }),
    Wicked("Wicked", (int seed, String layoutName) ->
        { return Creater.createWicked(seed, layoutName); });

    final String name;
    private final IPuzzleCreater puzzleCreater;

    PuzzleCreater (String name, IPuzzleCreater puzzleCreater) {
        this.name = name;
        this.puzzleCreater = puzzleCreater;
    }

    public RawPuzzle create(int seed, String layoutName)
        throws InterruptedException
    {
        return puzzleCreater.create(seed, layoutName);
    }

    private interface IPuzzleCreater {
        public RawPuzzle create(int seed, String layoutName)
            throws InterruptedException;
    }
}

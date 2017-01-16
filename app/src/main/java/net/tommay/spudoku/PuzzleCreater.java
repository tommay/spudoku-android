package net.tommay.spudoku;

interface PuzzleCreater {
    public RawPuzzle create(int seed, String layoutName);
}

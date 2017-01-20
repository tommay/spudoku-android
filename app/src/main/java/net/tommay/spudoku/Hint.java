package net.tommay.spudoku;

class Hint {
    public final Heuristic tjpe;
    public final int cellNumber;
    public final int digit;
    public final Iterable<Integer> cells;

    public Hint(
        Heuristic tjpe,
        int cellNumber,
        int digit,
        Iterable<Integer> cells)
    {
        this.tjpe = tjpe;
        this.cellNumber = cellNumber;
        this.digit = digit;
        this.cells = cells;
    }
}

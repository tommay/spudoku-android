package net.tommay.spudoku;

/**
 * Puzzle is just an array of Cells that maintain the state for the UI.
 */
class Puzzle {
    private final Cell[] _cells;

    Puzzle (Cell[] cells) {
        _cells = cells;
    }

    /**
     * Solved is the solution.
     * Setup has a '-' for each cell to be solved.
     */
    Puzzle (String setup, String solved) {
        this(makeCells(setup, solved));
    }

    private static Cell[] makeCells (String setup, String solved) {
        Cell[] cells = new Cell[81];
        for (int i = 0, len =  cells.length; i < len; i++) {
            // Digits range from 1 to 9 so subtract one to zero-base them.
            int digit = solved.charAt(i) - '0' - 1;
            boolean isSetup = setup.charAt(i) != '-';
            cells[i] = new Cell(digit, isSetup);
        }
        return cells;
    }

    Cell getCell(int  n) {
        return _cells[n];
    }

    Cell[] getCells() {
        return _cells;
    }
}

package net.tommay.spudoku;

class Puzzle {
    private final Cell[] _cells = new Cell[81];

    /**
     * Solved is the solution.
     * Setup has a '-' for each cell to be solved.
     */
    Puzzle (String setup, String solved) {
        for (int i = 0, len =  _cells.length; i < len; i++) {
            // Digits range from 1 to 9 so subtract one to zero-base them.
            int digit = solved.charAt(i) - '0' - 1;
            boolean isSetup = setup.charAt(i) != '-';
            _cells[i] = new Cell(digit, isSetup);
        }
    }

    Cell getCell(int  n) {
        return _cells[n];
    }
}

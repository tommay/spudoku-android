package net.tommay.spudoku;

class Puzzle {
    private static final int[] _colors = new int[] {
        0xff0000,               // red
        0xff9900,               // orange
        0xffff00,               // yellow
        0x33cc33,               // light green
        0x005900,               // dark green
        0x6db5f9,               // light blue
        0x0000cc,               // dark blue
        0xed82ed,               // lavender
        0x660082,               // purple
    };

    private final Cell[] _cells = new Cell[81];

    /**
     * solved is the solution
     * editmask has a "0" for each fixed value
     */
    Puzzle (String solved, String editmask) {
        for (int i = 0, len =  _cells.length; i < len; i++) {
            int color = _colors[(int)(solved.charAt(i) - '0' - 1)];
            _cells[i] = new Cell(color, editmask.charAt(i) == '0');
        }
    }

    Integer getColor(int  n) {
        return _cells[n].getColor();
    }
}

package net.tommay.spudoku;

class Cell {
    private final int _color;
    private final boolean _isSetup;
    private boolean _isSolved;

    Cell (int color, boolean isSetup) {
        _color = color;
        _isSetup = isSetup;
        _isSolved = false;
    }

    Integer getColor () {
        if (_isSetup || _isSolved) {
            return _color;
        }
        else {
            return null;
        }
    }

    void flip () {
        _isSolved = !_isSolved;
    }
}

package net.tommay.spudoku;

class Cell {
    private final int _digit;
    private final boolean _isSetup;
    private boolean _isPlaced;

    Cell (int digit, boolean isSetup) {
        _digit = digit;
        _isSetup = isSetup;
        _isPlaced = isSetup;
    }

    Integer getSetup() {
        return _isSetup ? _digit : null;
    }

    Integer getSolved() {
        return _digit;
    }

    Integer getPlaced() {
        return _isPlaced ? _digit : null;
    }

    void togglePlaced() {
        if (!_isSetup) {
            _isPlaced = !_isPlaced;
        }
    }
}

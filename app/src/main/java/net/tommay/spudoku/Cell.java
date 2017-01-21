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

    Integer getSetupDigit() {
        return _isSetup ? _digit : null;
    }

    Integer getSolvedDigit() {
        return _digit;
    }

    Integer getPlacedDigit() {
        return _isPlaced ? _digit : null;
    }

    boolean isPlaced() {
        return _isPlaced;
    }

    void setPlaced() {
        if (!_isSetup) {
            _isPlaced = true;
        }
    }

    void togglePlaced() {
        if (!_isSetup) {
            _isPlaced = !_isPlaced;
        }
    }
}

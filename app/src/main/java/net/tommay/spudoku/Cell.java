package net.tommay.spudoku;

class Cell {
    private final int _digit;
    private final boolean _isSetup;
    private Integer _placedDigit;
    private boolean _isGuess;

    Cell (int digit, boolean isSetup) {
        _digit = digit;
        _isSetup = isSetup;
        if (isSetup) {
            _placedDigit = _digit;
        }
        _isGuess = false;
    }

    boolean isSetup() {
        return _isSetup;
    }

    Integer getSetupDigit() {
        return _isSetup ? _digit : null;
    }

    Integer getSolvedDigit() {
        return _digit;
    }

    Integer getPlacedDigit() {
        return _placedDigit;
    }

    boolean isPlaced() {
        return _placedDigit != null;
    }

    void place(int digit) {
        _placedDigit = digit;
        setGuess(false);
    }

    void unplace() {
        _placedDigit = null;
        setGuess(false);
    }

    boolean isGuess() {
        return _isGuess;
    }

    void setGuess(boolean guess) {
        _isGuess = guess;
    }

    void toggleGuess() {
        _isGuess = !_isGuess;
    }
}

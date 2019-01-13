package net.tommay.spudoku;

class Cell {
    private final int _digit;
    private final boolean _isSetup;
    private boolean _isPlaced;
    private boolean _isGuess;

    Cell (int digit, boolean isSetup) {
        _digit = digit;
        _isSetup = isSetup;
        _isPlaced = isSetup;
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
        return _isPlaced ? _digit : null;
    }

    boolean isPlaced() {
        return _isPlaced;
    }

    void setPlaced(boolean placed) {
        _isPlaced = placed;
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

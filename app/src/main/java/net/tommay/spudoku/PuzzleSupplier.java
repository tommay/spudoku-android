package net.tommay.spudoku;

import java.io.PrintStream;

import java.util.concurrent.Callable;

import net.tommay.spudoku.RawPuzzle;

class PuzzleSupplier implements Callable<RawPuzzle> {
    private final PuzzleCreater _puzzleCreater;
    private final String _layoutName;
    private final PrintStream _log;

    public PuzzleSupplier (
        PuzzleCreater puzzleCreater,
        String layoutName,
        final PrintStream log)
    {
        _puzzleCreater = puzzleCreater;
        _layoutName = layoutName;
        _log = log;
    }

    public RawPuzzle call ()
        throws InterruptedException
    {
        long start = System.currentTimeMillis();
        try {
            // If we're logging create times then always
            // use the same seed for consistency.
            int seed = (_log == null) ? (int) start : 2;
            return _puzzleCreater.create(seed, _layoutName);
        }
        finally {
            if (_log != null) {
                long elapsed = System.currentTimeMillis() - start;
                _log.println(
                    new java.util.Date(start) + " : " +
                    _layoutName + ": " + elapsed);
            }
        }
    }
}

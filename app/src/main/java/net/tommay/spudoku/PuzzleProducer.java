package net.tommay.spudoku;

import java.io.PrintStream;

import net.tommay.spudoku.Creater;
import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.AOTState;
import net.tommay.util.Producer;
import net.tommay.util.ProducerException;

class PuzzleProducer implements Producer<RawPuzzle> {
    private final AOTProducer<RawPuzzle> _aotProducer;

    public PuzzleProducer (final String layoutName, AOTState aotState,
        final PrintStream log)
    {
        _aotProducer = new AOTProducer<RawPuzzle> (
            aotState,
            new Producer<RawPuzzle>() {
                @Override
                public RawPuzzle get() {
                    long start = System.currentTimeMillis();
                    try {
                        int seed = (int) start;
                        return Creater.create(seed, layoutName);
                    }
                    finally {
                        if (log != null) {
                            long elapsed = System.currentTimeMillis() - start;
                            log.println(
                                new java.util.Date(start) + " : " +
                                layoutName + ": " + elapsed);
                        }
                    }
                }
            });
    }

    @Override
    public RawPuzzle get () {
        try {
            return _aotProducer.get();
        }
        catch (ProducerException ex) {
            throw new RuntimeException(ex);
        }
    }
}

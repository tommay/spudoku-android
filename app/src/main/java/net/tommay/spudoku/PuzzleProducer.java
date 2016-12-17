package net.tommay.spudoku;

import net.tommay.spudoku.Creater;
import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.AOTState;
import net.tommay.util.Producer;
import net.tommay.util.ProducerException;

class PuzzleProducer implements Producer<RawPuzzle> {
    private final AOTProducer<RawPuzzle> _aotProducer;

    public PuzzleProducer (final String layoutName, AOTState aotState) {
        _aotProducer = new AOTProducer<RawPuzzle> (
            aotState,
            new Producer<RawPuzzle>() {
                @Override
                public RawPuzzle get() {
                    int seed = (int) System.currentTimeMillis();
                    return Creater.create(seed, layoutName);
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

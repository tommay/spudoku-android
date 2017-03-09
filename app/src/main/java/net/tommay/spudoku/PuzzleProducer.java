package net.tommay.spudoku;

import java.io.PrintStream;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.Producer;

class PuzzleProducer implements Producer<RawPuzzle> {
    private static final ExecutorService _executorService =
        Executors.newSingleThreadExecutor();

    private final PuzzleCreater _puzzleCreater;
    private final String _layoutName;
    private final PrintStream _log;

    public PuzzleProducer (
        PuzzleCreater puzzleCreater,
        String layoutName,
        final PrintStream log)
    {
        _puzzleCreater = puzzleCreater;
        _layoutName = layoutName;
        _log = log;
    }

    @Override
    public RawPuzzle get ()
        throws InterruptedException, TimeoutException
    {
        // Future.cancel returns immediately without waiting for its
        // Callable to finish, so use a semaphore that the Callable
        // releases in a finally before it completes for any reason.

        final Semaphore done = new Semaphore(0);

        // Fire off a Callable to do the real work, while this thread,
        // which is a background thread, waits to get the result or
        // time it out.

        Callable<RawPuzzle> worker = new Callable<RawPuzzle>() {
            @Override
            public RawPuzzle call() {
                try {
                    return getRawPuzzle();
                }
                catch (InterruptedException ex) {
                    // Return value is unused.
                    return null;
                }
                finally {
                    done.release();
                }
            }
        };

        Future<RawPuzzle> future = _executorService.submit(worker);

        try {
            return future.get(3, TimeUnit.SECONDS);
        }
        catch (TimeoutException|InterruptedException ex) {
            // We were cancelled.  Cancel the worker, too.
            // Or we weren't cancelled, but the worker is taking too
            // long so cancel it.  In either case it will exit and
            // the return value is unused.
            future.cancel(true);
            // Wait for the worker to finish and release the Semaphore.
            try {
                done.acquire();
            }
            catch (InterruptedException ex2) {
                throw new RuntimeException("Shouldn't happen: ", ex2);
            }
            throw ex;
        }
        catch (ExecutionException ex) {
            throw new RuntimeException("Shouldn't happen: ", ex);
        }
    }

    // This is executed in a worker and timed out by the main
    // background thread.

    private RawPuzzle getRawPuzzle ()
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

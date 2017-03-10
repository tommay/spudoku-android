package net.tommay.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * A Callable wrapper than runs the Callable with a timeout.  The
 * wrapped Callable will be executed in a worker thread, the timout
 * code runs in the thread that called WithTimeout#call.  Currently
 * there is only one worker thread so Callables will be run serially.
 *
 * If the Callable exceeds the timeout it is cancelled/interrupted and
 * waited for then WithTimeout#call throws TimeoutException.  So the Callable
 * should handle interrupts responsively.
 *
 * If WithTimeout#call is cancelled, it will cancel the Callable and wait for
 * it then throw InterruptedException.
 */
public class WithTimeout<T> implements Callable<T> {
    private static final ExecutorService _executorService =
        Executors.newSingleThreadExecutor();

    private final Callable<T> _wrappedCallable;
    private final long _timeoutMillis;

    public WithTimeout (Callable<T> callable, long timeoutMillis) {
        _wrappedCallable = callable;
        _timeoutMillis = timeoutMillis;
    }

    @Override
    public T call ()
        throws InterruptedException, TimeoutException
    {
        // Future.cancel returns immediately without waiting for its
        // Callable to finish, so use a semaphore that the Callable
        // releases in a finally before it completes for any reason.

        final Semaphore done = new Semaphore(0);

        // Fire off whis worker Callable to do the real work, while
        // this thread, which is a background thread, waits for its
        // result and returns it, or times it out.

        Callable<T> worker = new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    return _wrappedCallable.call();
                }
                catch (InterruptedException ex) {
                    // worker was cancelled and in turn cancelled
                    // _wrappedCallable.  This return value is unused
                    // because WithTimeout#call is going to throw an
                    // Exception.
                    return null;
                }
                finally {
                    done.release();
                }
            }
        };

        Future<T> future = _executorService.submit(worker);

        try {
            return future.get(_timeoutMillis, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException|InterruptedException ex) {
            // This thread was cancelled.  Cancel the worker, too.  Or
            // we weren't cancelled, but the worker is taking too long
            // so cancel it.  In either case it will exit and the
            // return value is unused.
            future.cancel(true);
            // Wait for the worker release the Semaphore when it finishes.
            try {
                done.acquire();
            }
            catch (InterruptedException ex2) {
                // It's possible to be interrupted during done.acquire
                // if future.get timed out and then we were cancelled.
                // So just ignore it.
            }
            throw ex;
        }
        catch (ExecutionException ex) {
            throw new RuntimeException("Shouldn't happen: ", ex);
        }
    }
}

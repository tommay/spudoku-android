package net.tommay.spudoku;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

import net.tommay.util.Producer;
import net.tommay.util.ProducerException;

class AOTProducer<T> implements Producer {
    private static final ExecutorService _executor =
        Executors.newSingleThreadExecutor();  // XXX use a factory

    private final Callable<T> _callable;
    private volatile Future<T> _future;

    public AOTProducer (final T initial, final Producer<T> producer)
    {
        // Start with a Future that returns initial (which may be
        // null).  Each call to get will create a new Future that
        // returns the result of producer.get via _callable.

        _future = _executor.submit(
            new Runnable () {
                @Override public void run () {}
            },
            initial);

        _callable = new Callable<T>() {
            @Override
            public T call () throws ProducerException {
                return producer.get();
            }
        };
    }

    @Override
    public T get () throws ProducerException
    {
        try {
            T result = _future.get();
            // The Callable is only re-submitted if it succeeded, else
            // we throw the exception every time.
            _future = _executor.submit(_callable);
            return result;
        }
        catch (InterruptedException|ExecutionException ex) {
            throw new ProducerException(ex);
        }
    }

    /**
     * Return the object ready for get to return, or null if nothing
     * is ready.  This is used to save state used to create a new
     * AOTProducer in the future with the same AOT-created object.
     */
    public T peek () {
        try {
            return _future.get(0, TimeUnit.SECONDS);
        }
        catch (InterruptedException|ExecutionException|TimeoutException ex) {
            return null;
        }
    }
}

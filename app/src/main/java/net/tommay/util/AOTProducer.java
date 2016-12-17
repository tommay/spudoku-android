package net.tommay.spudoku;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.tommay.util.Producer;
import net.tommay.util.ProducerException;

class AOTProducer<T> implements Producer {
    private static final ExecutorService _executor =
        Executors.newSingleThreadExecutor();  // XXX use a factory

    private final Callable<T> _callable;
    private volatile Future<T> _future;

    public AOTProducer (final T initial, final Producer<T> producer)
    {
        _callable = new Callable<T>() {
            @Override
            public T call () throws ProducerException {
                return producer.get();
            }
        };

        _future = _executor.submit(
            new Runnable () {
                @Override public void run () {}
            },
            initial);
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
        catch (Throwable ex) {
            throw new ProducerException(ex);
        }
    }
}

package net.tommay.spudoku;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.tommay.util.AOTState;
import net.tommay.util.Producer;
import net.tommay.util.ProducerException;

class AOTProducer<T> implements Producer {
    private static final ExecutorService _executor =
        Executors.newSingleThreadExecutor();  // XXX use a factory

    private final AOTState<T> _state;
    private final Callable<T> _callable;
    private volatile Future<T> _future;

    public AOTProducer (AOTState<T> state, final Producer<T> producer) {
        _state = state;

        final T initial = _state.get();
        
        // Start with a Future that returns initial (which may be
        // null).  Each call to get will create a new Future that
        // returns the result of producer.get via _callable, which
        // also persists result in _state.

        _future = _executor.submit(
            new Runnable () {
                @Override public void run () {}
            },
            initial);

        _callable = new Callable<T>() {
            @Override
            public T call () throws ProducerException {
                T result = producer.get();
                _state.put(result);
                return result;
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
            if (result != null) {
                return result;
            }
            else {
                return get();
            }
        }
        catch (InterruptedException|ExecutionException ex) {
            throw new ProducerException(ex);
        }
    }
}

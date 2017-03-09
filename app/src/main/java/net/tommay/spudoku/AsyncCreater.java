package net.tommay.spudoku;

import android.os.AsyncTask;

import java.util.concurrent.TimeoutException;

import net.tommay.util.Callback;
import net.tommay.util.Producer;

// https://developer.android.com/reference/android/os/AsyncTask.html

class AsyncCreater<T> {
    private AsyncCreater () {
        // No instantiation.
    }

    public static <T> Handle create(
        final Producer<T> producer,
        final Callback<T> consumer,
        final Callback<Void> cancel,
        final Callback<Void> timeout)
    {
        AsyncTask<Void, Void, T> asyncTask = new AsyncTask<Void, Void, T>() {
            // Backround thread.
            @Override
            protected T doInBackground(Void[] v) {
                try {
                    return producer.get();
                }
                catch (InterruptedException ex) {
                    android.util.Log.i("Spudoku", "doInBackground interrupted");
                    // We were cancelled.  The return value isn't used.
                    return null;
                }
                catch (TimeoutException ex) {
                    // We can't throw a checked Exception so return
                    // null to indicate timeout.
                    return null;
                }
            }

            // UI thread.
            @Override
            protected void onPostExecute(T result) {
                // result is null on timeout.
                if (result != null) {
                    consumer.call(result);
                }
                else {
                    timeout.call(null);
                }
            }

            // UI thread.
            @Override
            protected void onCancelled(T result) {
                cancel.call(null);
            }
        };

        asyncTask.execute();

        return new Handle(asyncTask);
    }

    public static class Handle {
        private final AsyncTask _asyncTask;

        Handle (AsyncTask asyncTask) {
            _asyncTask = asyncTask;
        }

        public void cancel () {
            _asyncTask.cancel(true);
        }
    }
}

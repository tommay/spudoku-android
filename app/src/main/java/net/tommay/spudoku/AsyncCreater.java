package net.tommay.spudoku;

import android.os.AsyncTask;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import net.tommay.util.Callback;

// https://developer.android.com/reference/android/os/AsyncTask.html

class AsyncCreater<T> {
    private AsyncCreater () {
        // No instantiation.
    }

    public static <T> Handle create(
        final Callable<T> supplier,
        final Callback<T> consumer,
        final Callback<Void> cancel,
        final Callback<Void> timeout)
    {
        AsyncTask<Void, Void, T> asyncTask = new AsyncTask<Void, Void, T>() {
            // Backround thread.
            @Override
            protected T doInBackground(Void[] v) {
                try {
                    return supplier.call();
                }
                catch (InterruptedException ex) {
                    android.util.Log.i("Spudoku", "doInBackground interrupted");
                    // We were cancelled.  The return value isn't used.
                    return null;
                }
                catch (TimeoutException ex) {
                    // We can't throw a checked Exception from
                    // doInBackground so return null to indicate
                    // timeout.
                    return null;
                }
                catch (Exception ex) {
                    throw new RuntimeException("Shouldn't happen: ", ex);
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

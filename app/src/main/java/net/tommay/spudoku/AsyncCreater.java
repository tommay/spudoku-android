package net.tommay.spudoku;

import android.os.AsyncTask;

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
        final Callback<Void> cancel)
    {
        AsyncTask<Void, Void, T> asyncTask = new AsyncTask<Void, Void, T>() {
            // Backround thread.
            @Override
            public T doInBackground(Void[] v) {
                return producer.get();
            }

            // UI thread.
            @Override
            public void onPostExecute(T result) {
                consumer.call(result);
            }

            // UI thread.
            @Override
            public void onCancelled(T result) {
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

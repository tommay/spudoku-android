package net.tommay.spudoku;

import android.os.AsyncTask;

import net.tommay.util.Callback;
import net.tommay.util.Producer;

// https://developer.android.com/reference/android/os/AsyncTask.html

class AsyncCreater<T> {
    private AsyncCreater () {
        // No instantiation.
    }

    public static <T> void create(
        final Producer<T> producer, final Callback<T> callback)
    {
        new AsyncTask<Void, Void, T>() {
            // Backround thread.
            @Override
            public T doInBackground(Void[] v) {
                return producer.get();
            }

            // UI thread.
            @Override
            public void onPostExecute(T result) {
                callback.call(result);
            }
        }.execute();
    }
}

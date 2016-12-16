package net.tommay.spudoku;

import java.lang.System;

import android.os.AsyncTask;

import net.tommay.spudoku.Creater;
import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.Consumer;

// https://developer.android.com/reference/android/os/AsyncTask.html

class AsyncCreater {
    private AsyncCreater () {
        // No instantiation.
    }

    public static void create(
        final String layoutName, final Consumer<RawPuzzle> consumer)
    {
        new AsyncTask<Void, Void, RawPuzzle>() {
            @Override
            public RawPuzzle doInBackground(Void[] v) {
                int seed = (int) System.currentTimeMillis();
                return Creater.create(seed, layoutName);
            }

            @Override
            public void onPostExecute(RawPuzzle puzzle) {
                consumer.accept(puzzle);
            }
        }.execute();
    }
}

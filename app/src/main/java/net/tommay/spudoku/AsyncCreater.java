package net.tommay.spudoku;

import java.lang.System;

import android.os.AsyncTask;

import net.tommay.spudoku.Creater;
import net.tommay.spudoku.Puzzle;
import net.tommay.util.Consumer;

// https://developer.android.com/reference/android/os/AsyncTask.html

class AsyncCreater {
    private AsyncCreater () {
        // No instatntiation.
    }

    public static void create(
        final String layoutName, final Consumer<Puzzle> consumer)
    {
        new AsyncTask<Void, Void, Puzzle>() {
            @Override
            public Puzzle doInBackground(Void[] v) {
                int seed = (int) System.currentTimeMillis();
                String[] puzzleStrings = Creater.create(seed, layoutName);
                return new Puzzle(
                    puzzleStrings[0],
                    puzzleStrings[1]);
            }

            @Override
            public void onPostExecute(Puzzle puzzle) {
                consumer.accept(puzzle);
            }
        }.execute();
    }
}

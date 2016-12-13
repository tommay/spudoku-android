package net.tommay.spudoku;

import java.lang.System;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.tommay.spudoku.Puzzle;
import net.tommay.spudoku.Creater;

public class MainActivity extends AppCompatActivity {
    private int _emptyCellColor;

    private Puzzle _puzzle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Spudoku", "onCreate");
        Resources res = getResources();
        _emptyCellColor = res.getColor(R.color.emptyCell);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Spudoku", "onStart");
        _puzzle = newPuzzle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Spudoku", "onResume");
        showBoard();
    }

    // This is pretty awful.

    private void showBoard () {
        View view = findViewById(R.id.board);
        for (int i = 0; i < 81; i++) {
            ImageView cell =
                (ImageView)view.findViewWithTag(Integer.toString(i));
            colorCell(cell, _puzzle, i);
        }
    }

    private void colorCell(ImageView cell, Puzzle puzzle, int n)
    {
        GradientDrawable drawable = (GradientDrawable)cell.getDrawable();
        //Log.i("Spudoku", "drawable: " + drawable);

        Integer color = puzzle.getColor(n);
        int c = color != null ? color : _emptyCellColor;
        drawable.setColor(0xFF000000 | c);
    }

    public void clicked(View view) {
        String tag = (String)view.getTag();
        Log.i("Spudoku", "clicked " + tag);

        int n = Integer.parseInt(tag);

        _puzzle.flip(n);

        colorCell((ImageView)view, _puzzle, n);
    }

    private Puzzle newPuzzle() {
        int seed = (int) System.currentTimeMillis();
        String[] puzzleStrings = Creater.create(seed, "classic");
        return new Puzzle(
            puzzleStrings[0],
            puzzleStrings[1]);
    }
    
    public void clickNew(View view) {
        Log.i("Spudoku", "new");
        _puzzle = newPuzzle();
        showBoard();
    }

    public void clickSetup(View view) {
        Log.i("Spudoku", "setup");
        _puzzle.setup();
        showBoard();
    }

    public void clickSolved(View view) {
        Log.i("Spudoku", "solved");
        _puzzle.solved();
        showBoard();
    }
}

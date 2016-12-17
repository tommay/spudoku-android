package net.tommay.spudoku;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.tommay.spudoku.AOTStateImpl;
import net.tommay.spudoku.AsyncCreater;
import net.tommay.spudoku.Puzzle;
import net.tommay.spudoku.PuzzleProducer;
import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.Consumer;
import net.tommay.util.Producer;

public class MainActivity extends AppCompatActivity {
    // Keys for Bundle values.

    private static final String KEY_PUZZLE = "puzzle";
    private static final String KEY_SOLUTION = "solution";

    private int _emptyCellColor;

    private RawPuzzle _rawPuzzle = null;
    private Puzzle _puzzle = null;

    private PuzzleProducer _puzzleProducer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Spudoku", "onCreate");
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        _emptyCellColor = res.getColor(R.color.emptyCell);

        _puzzleProducer = new PuzzleProducer(
            "classic",
            AOTStateImpl.create(
                getSharedPreferences("classic", 0),
                new RawPuzzle(
                    "----15-4-3-----56-5--6----98-5-436" +
                    "-------------752-9-47----4--2-51-----7-3-15----",
                    "6798152433124795685846327198259436" +
                    "71943761825167528934796384152451296387238157496")));

        if (savedInstanceState != null) {
            String puzzle = savedInstanceState.getString(KEY_PUZZLE);
            String solution = savedInstanceState.getString(KEY_SOLUTION);
            if (puzzle != null && solution != null) {
                Log.i("Spudoku", "restoring from bundle");
                _rawPuzzle = new RawPuzzle(puzzle, solution);
                _puzzle = newPuzzle(_rawPuzzle);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Spudoku", "onStart");
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i("Spudoku", "onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Spudoku", "onResume");
        showBoard();
    }

    // Called before onStop, either before or after onPause.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("Spudoku", "onSaveInstanceState");
        if (_rawPuzzle != null) {
            Log.i("Spudoku", "saving state to bundle");
            outState.putString(KEY_PUZZLE, _rawPuzzle.puzzle);
            outState.putString(KEY_SOLUTION, _rawPuzzle.solution);
        }
    }

    @Override
    protected void onPause () {
        super.onPause();
        Log.i("Spudoku", "onPause");
    }

    @Override
    protected void onStop () {
        super.onStop();
        Log.i("Spudoku", "onStop");
    }

    // Called betweeb onStop and onStart.
    @Override
    protected void onRestart () {
        super.onRestart();
        Log.i("Spudoku", "onRestart");
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        Log.i("Spudoku", "onDestroy");
    }

    // This is pretty awful.

    private void showBoard () {
        if (_puzzle != null) {
            View view = findViewById(R.id.board);
            for (int i = 0; i < 81; i++) {
                ImageView cell =
                    (ImageView)view.findViewWithTag(Integer.toString(i));
                colorCell(cell, _puzzle, i);
            }
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

    private void enableButtons(boolean enabled) {
        int[] buttonIds = {
            R.id.button_new,
            R.id.button_setup,
            R.id.button_solved,
        };
        for (int i = 0; i < buttonIds.length; i++) {
            View view = findViewById(buttonIds[i]);
            view.setEnabled(enabled);
        }
    }

    public void clickNew(View view) {
        Log.i("Spudoku", "new");
        enableButtons(false);
        AsyncCreater.<RawPuzzle>create(
            _puzzleProducer,
            new Consumer<RawPuzzle>() {
                @Override
                public void accept(RawPuzzle rawPuzzle) {
                    _rawPuzzle = rawPuzzle;
                    _puzzle = newPuzzle(_rawPuzzle);
                    showBoard();
                    enableButtons(true);
                }
            });
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

    private static Puzzle newPuzzle(RawPuzzle rawPuzzle) {
        return new Puzzle(rawPuzzle.puzzle, rawPuzzle.solution);
    }
}

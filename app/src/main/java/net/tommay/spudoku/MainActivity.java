package net.tommay.spudoku;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

// Auto-generated from aoo/build.gradle settings.
import net.tommay.spudoku.BuildConfig;

import net.tommay.spudoku.AOTStateImpl;
import net.tommay.spudoku.AsyncCreater;
import net.tommay.spudoku.LayoutNames;
import net.tommay.spudoku.Puzzle;
import net.tommay.spudoku.PuzzleProducer;
import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.Consumer;
import net.tommay.util.Producer;

public class MainActivity extends AppCompatActivity {
    // Keys for Bundle values.

    private static final String KEY_PUZZLE = "puzzle";
    private static final String KEY_SOLUTION = "solution";

    // Context-dependent "constants".

    // This comes from main/res/values/colors.xml.  We have to wait to
    // ge it until an instance method is called so we have a Context
    // to find it in.

    private int _emptyCellColor;

    // Map from layout names to the PuzzleProducer for that layout.
    // To fill te Map in we need our context.

    private final Map<String, PuzzleProducer> _producerMap = new HashMap();

    // True variables for state.

    private RawPuzzle _rawPuzzle = null;
    private Puzzle _puzzle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Spudoku", "onCreate");

        setContentView(R.layout.activity_main);

        // Initialize Context-dependent _emptyCellColor.

        Resources res = getResources();
        _emptyCellColor = res.getColor(R.color.emptyCell);

        // Initialize Context-dependent _producerMap.

        List<String> layoutNames = LayoutNames.getLayoutNames();

        // If configured in build.gradle, log puzzle create rimes to
        // /data/data/net.tommay.spudoku/files/<CREATE_LOG>.

        PrintStream log = null;
        if (BuildConfig.CREATE_LOG != null) {
            try {
                log = new PrintStream(
                    openFileOutput(BuildConfig.CREATE_LOG,
                        MODE_PRIVATE | MODE_APPEND),
                    true /* autoflush */);
            }
            catch (FileNotFoundException ex) {
                // WTF.
            }
        }

        for (String layoutName : layoutNames) {
            PuzzleProducer puzzleProducer = new PuzzleProducer(
                layoutName,
                AOTStateImpl.create(
                    getSharedPreferences(layoutName, 0),
                    new RawPuzzle(
                        "----15-4-3-----56-5--6----98-5-436" +
                        "-------------752-9-47----4--2-51-----7-3-15----",
                        "6798152433124795685846327198259436" +
                        "71943761825167528934796384152451296387238157496")),
                log);
            _producerMap.put(layoutName, puzzleProducer);
        }

        // Initialize the layout spinner with the layout names.

        Spinner spinner = (Spinner) findViewById(R.id.spinner_layout);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, layoutNames);
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Restore stuff from savedInstanceState.

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

    // clicked is called from the circle images in
    // main/res/layout/board.xml.  Then a circle is clicked, we flip
    // it between its solved and setup colors.

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
        for (int buttonId : buttonIds) {
            View view = findViewById(buttonId);
            view.setEnabled(enabled);
        }
    }

    // The new button was clicked.  Set up a callback for when we have
    // a Puzzle.

    public void clickNew(View view) {
        Log.i("Spudoku", "new");

        // Retrieve the select layout from the layout spinner, and get
        // the corrresponding puzzleProducer.

        Spinner spinner = (Spinner) findViewById(R.id.spinner_layout);
        String layoutName = (String) spinner.getSelectedItem();
        PuzzleProducer puzzleProducer = _producerMap.get(layoutName);

        // Disable the buttons until we have a puzzle.  They are
        // re-enabled in the callback.

        enableButtons(false);

        // Set up a callback for when we have a Puzzle.

        AsyncCreater.<RawPuzzle>create(
            puzzleProducer,
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

    // The setup button was clicked.  Show the setup colors.

    public void clickSetup(View view) {
        Log.i("Spudoku", "setup");
        _puzzle.setup();
        showBoard();
    }

    // The solved button was clicked.  Show the solved colors.

    public void clickSolved(View view) {
        Log.i("Spudoku", "solved");
        _puzzle.solved();
        showBoard();
    }

    // Create a Puzzle from a RawPuzzle.  This is the only code that
    // knows about both classes.

    private static Puzzle newPuzzle(RawPuzzle rawPuzzle) {
        return new Puzzle(rawPuzzle.puzzle, rawPuzzle.solution);
    }
}

package net.tommay.spudoku;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

// Auto-generated from aoo/build.gradle settings.
import net.tommay.spudoku.BuildConfig;

import net.tommay.spudoku.AOTStateImpl;
import net.tommay.spudoku.AsyncCreater;
import net.tommay.spudoku.Heuristic;
import net.tommay.spudoku.Hint;
import net.tommay.spudoku.LayoutNames;
import net.tommay.spudoku.Puzzle;
import net.tommay.spudoku.PuzzleProducer;
import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.Consumer;
import net.tommay.util.Producer;

import net.tommay.spudoku.EasyCreater;
import net.tommay.spudoku.ViciousCreater;
import net.tommay.spudoku.WickedCreater;
import net.tommay.spudoku.Hinter;

public class MainActivity extends AppCompatActivity {
    private enum Showing {
        SETUP, SOLVED, PLACED,
    };

    // Keys for Bundle values.

    private static final String KEY_PUZZLE = "puzzle";
    private static final String KEY_SOLUTION = "solution";

    private static final int[] _colors = new int[] {
        0xee0000,               // red
        0xdc9b40,               // orange
        0xf9f911,               // yellow
        0x3bee33,               // light green
        0x1a691a,               // dark green
        0x2929cc,               // dark blue
        0x6db5f9,               // light blue
        0xed82ed,               // lavender
        0x710091,               // purple
    };
    private static final String[] _colorNames = new String[] {
        "red", "orange", "yellow", "light green", "dark green",
        "dark blue", "light blue", "lavender", "purple"
    };

    // Difficulty ratings for creating puzzles, and associated
    // PuzzleCreater.

    private final Map<String, PuzzleCreater> _ratingsMap =
        new LinkedHashMap(){{
            put("Easy", new EasyCreater());
            put("Vicious", new ViciousCreater());
            put("Wicked", new WickedCreater());
        }};

    // Context-dependent "constants".

    // This comes from main/res/values/colors.xml.  We have to wait to
    // get it until this object has been constructed to give us a
    // Context for accessing resources.

    private int _emptyCellColor;

    // Map from ratings + layout names to the PuzzleProducer for that
    // combination.  To fill the Map in we need our Context.

    private final Map<String, PuzzleProducer> _producerMap = new HashMap();

    // True variables for state.

    private RawPuzzle _rawPuzzle = null;
    private Puzzle _puzzle = null;
    private Showing _showing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Spudoku", "onCreate");

        setContentView(R.layout.activity_main);

        // Initialize Context-dependent _emptyCellColor.

        Resources res = getResources();
        _emptyCellColor = res.getColor(R.color.emptyCell);

        // If configured in build.gradle, log puzzle create times to
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

        List<String> layoutNames = LayoutNames.getLayoutNames();

        // Initialize Context-dependent _producerMap.

        for (Map.Entry<String,PuzzleCreater> entry : _ratingsMap.entrySet()) {
            String rating = entry.getKey();
            PuzzleCreater puzzleCreater = entry.getValue();
            
            for (String layoutName : layoutNames) {
                String producerName = makeProducerName(rating, layoutName);
                PuzzleProducer puzzleProducer = new PuzzleProducer(
                    puzzleCreater,
                    layoutName,
                    AOTStateImpl.create(
                        getSharedPreferences(producerName, 0),
                        new RawPuzzle(
                            "----15-4-3-----56-5--6----98-5-436-------" +
                            "------752-9-47----4--2-51-----7-3-15----",
                            "67981524331247956858463271982594367194376" +
                            "1825167528934796384152451296387238157496")),
                    log);
                _producerMap.put(producerName, puzzleProducer);
            }
        }

        // Initialize the layout spinner with the layout names.

        {
            Spinner spinner = (Spinner) findViewById(R.id.spinner_layout);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, layoutNames);
            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        // Initialize the rating spinner.

        {
            Spinner spinner = (Spinner) findViewById(R.id.spinner_rating);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                new ArrayList(_ratingsMap.keySet()));
            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        _showing = Showing.PLACED;

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
        clearHint();
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

    private void showBoard () {
        if (_puzzle != null) {
            View boardView = findViewById(R.id.board);
            for (int i = 0; i < 81; i++) {
                ImageView cellView =
                    (ImageView)boardView.findViewWithTag(Integer.toString(i));
                showCell(cellView, _puzzle.getCell(i));
            }
        }
    }

    private void showCell(ImageView cellView, Cell cell) {
        // It would be nicer to use an interface with a getCellColor
        // method but it's painfully verbose so just live with switch.
        Integer digit = null;
        switch (_showing) {
          case SETUP:
            digit = cell.getSetupDigit();
            break;
          case SOLVED:
            digit = cell.getSolvedDigit();
            break;
          case PLACED:
            digit = cell.getPlacedDigit();
            break;
        }
        int color = digit != null ? _colors[digit] : _emptyCellColor;

        GradientDrawable drawable = (GradientDrawable)cellView.getDrawable();
        drawable.setColor(0xFF000000 | color);
    }

    // clicked is called from the circle images in
    // main/res/layout/board.xml.  When a circle is clicked, we toggle
    // its cell between its solved and setup colors.

    public void clicked(View cellView) {
        String tag = (String)cellView.getTag();
        Log.i("Spudoku", "clicked " + tag);

        int n = Integer.parseInt(tag);
        Cell cell = _puzzle.getCell(n);
        switch (_showing) {
          case PLACED:
            cell.togglePlaced();
            showCell((ImageView)cellView, cell);
            break;
          case SETUP:
            cell.setPlaced();
            showPlaced();
            break;
          case SOLVED:
            showPlaced();
            break;
        }

        clearHint();
    }

    private void enableButtons(boolean enabled) {
        int[] buttonIds = {
            R.id.button_new,
            R.id.button_setup,
            R.id.button_solved,
            R.id.button_hint,
        };
        for (int buttonId : buttonIds) {
            View view = findViewById(buttonId);
            view.setEnabled(enabled);
        }
    }

    private void highlightButton(int highlightButtonId) {
        int[] buttonIds = {
            R.id.button_setup,
            R.id.button_solved,
        };
        for (int buttonId : buttonIds) {
            View view = findViewById(buttonId);
            if (buttonId == highlightButtonId) {
                view.getBackground().setColorFilter(
                    new LightingColorFilter(0xFF808080, 0xFF008000));
            }
            else {
                view.getBackground().clearColorFilter();
            }
        }
    }

    // The new button was clicked.  Set up a callback for when we have
    // a Puzzle.

    public void clickNew(View view) {
        Log.i("Spudoku", "new");

        // Retrieve the select layout from the layout spinner, and get
        // the corrresponding puzzleProducer.

        String layoutName = getSpinnerItem(R.id.spinner_layout);
        String rating = getSpinnerItem(R.id.spinner_rating);
        PuzzleProducer puzzleProducer =
            _producerMap.get(makeProducerName(rating, layoutName));

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
                    showPlaced();
                    enableButtons(true);
                }
            });
    }

    private String getSpinnerItem(int viewId) {
        Spinner spinner = (Spinner) findViewById(viewId);
        return (String) spinner.getSelectedItem();
    }

    private static String makeProducerName(String rating, String layoutName) {
        return rating + "-" + layoutName;
    }

    // The setup button was clicked.  Show the setup colors.

    public void clickSetup(View view) {
        Log.i("Spudoku", "setup");

        if (_showing != Showing.SETUP) {
            showSetup();
        }
        else {
            showPlaced();
        }
    }

    // The solved button was clicked.  Toggle between the user state
    // and the solved state.

    public void clickSolved(View view) {
        Log.i("Spudoku", "solved");

        if (_showing != Showing.SOLVED) {
            showSolved();
        }
        else {
            showPlaced();
        }
    }

    private void showSetup() {
        _showing = Showing.SETUP;
        highlightButton(R.id.button_setup);
        showBoard();
        clearHint();
    }

    private void showSolved() {
        _showing = Showing.SOLVED;
        highlightButton(R.id.button_solved);
        showBoard();
        clearHint();
    }

    private void showPlaced() {
        _showing = Showing.PLACED;
        highlightButton(-1); 
        showBoard();
        clearHint();
    }

    public void clickHint(View view) {
        int seed = (int) System.currentTimeMillis();
        String puzzleString = getPuzzleString();
        Hint hint = Hinter.getHint(seed, puzzleString);

        showPlaced();
        clearHint();

        if (hint != null) {
            if (hint.tjpe == Heuristic.EasyPeasy) {
                showHint("Easy peasy " + _colorNames[hint.digit - 1]);
            }
            else {
                showHint(hint.tjpe.toString());
                View boardView = findViewById(R.id.board);
                for (int cell : hint.cells) {
                    ImageView cellView = (ImageView)
                        boardView.findViewWithTag(Integer.toString(cell));
                    cellView.setBackgroundColor(0xFF989898);
                }
            }
        }
        else {
            showHint("Solved!");
        }
    }

    private String getPuzzleString() {
        StringBuilder sb = new StringBuilder(81);
        for (int i = 0; i < 81; i++) {
            Cell cell = _puzzle.getCell(i);
            Integer digit = cell.getPlacedDigit();
            if (digit != null) {
                sb.append(digit + 1);
            }
            else {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    private void showHint(String hint) {
        TextView textView = (TextView) findViewById(R.id.hint_text);
        textView.setText(hint);
    }

    private void clearHint() {
        showHint("");
        View boardView = findViewById(R.id.board);
        for (int i = 0; i < 81; i++) {
            ImageView cellView =
                (ImageView) boardView.findViewWithTag(Integer.toString(i));
            cellView.setBackgroundColor(0xFFD0D0D0);
        }
    }

    // Create a Puzzle from a RawPuzzle.  This is the only code that
    // knows about both classes.

    private static Puzzle newPuzzle(RawPuzzle rawPuzzle) {
        return new Puzzle(rawPuzzle.puzzle, rawPuzzle.solution);
    }
}

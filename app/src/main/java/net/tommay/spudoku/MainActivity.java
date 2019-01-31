package net.tommay.spudoku;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.LightingColorFilter;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// Auto-generated from aoo/build.gradle settings.
import net.tommay.spudoku.BuildConfig;

import net.tommay.spudoku.AsyncCreater;
import net.tommay.spudoku.Heuristic;
import net.tommay.spudoku.Hint;
import net.tommay.spudoku.LayoutNames;
import net.tommay.spudoku.Puzzle;
import net.tommay.spudoku.PuzzleCreater;
import net.tommay.spudoku.PuzzleSupplier;
import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.Callback;
import net.tommay.util.WithTimeout;

import net.tommay.spudoku.Hinter;

public class MainActivity
    extends AppCompatActivity
{
    // The release buid for publishing should not have log statements.
    // They can be removed by ProGuard, ut that requires optimization
    // to be turned on which breaks things.  So ise the low-tech
    // approach and check a boolean when logging.  The code is uglier,
    // but it always works.

    private static final boolean LOG = net.tommay.spudoku.Log.LOG;
    private static final String TAG = net.tommay.spudoku.Log.TAG;

    private enum Showing {
        SETUP, SOLVED, PLACED,
    };

    // Keys for Bundle values.

    private static final String KEY_PUZZLE = "puzzle";

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

    // _guessColors is used for the question mark displayed on
    // guesses.  it should be a reasonably contrasting color with the
    // circle color.

    private static final int[] _guessColors = new int[] {
        0xcccccc,               // red
        0x444444,               // orange
        0x444444,               // yellow
        0x444444,               // light green
        0xcccccc,               // dark green
        0xcccccc,               // dark blue
        0x444444,               // light blue
        0x444444,               // lavender
        0xcccccc,               // purple
    };

    // Difficulty ratings for creating puzzles, and associated
    // PuzzleCreater.

    private final Map<String, PuzzleCreater> _difficultyMap =
        new LinkedHashMap(){{
            for (PuzzleCreater puzzleCreater : PuzzleCreater.values()) {
                put(puzzleCreater.name, puzzleCreater);
            }
        }};

    // Context-dependent "constants".

    // Colors are defined in main/res/values/colors.xml.  We have to
    // wait to get them until this object has been constructed to give
    // us a Context for accessing resources.

    private int _emptyCellColor;
    private int _noPuzzleCellColor;
    private int _hintHighlightColor;

    // Map from difficulty + layout names to the PuzzleSupplier for that
    // combination.  To fill the Map in we need our Context.

    private final Map<String, PuzzleSupplier> _supplierMap = new HashMap();

    // Views of the color circles.  These are made INVISIBLE/VISIBLE
    // according to whether th corresponding color count is zero, i.e.,
    // there are no more of that color to place.

    private final View[] _colorViews = new View[9];

    // True variables for state.  They are accessed only from the UI thread.

    private Puzzle _puzzle = null;
    private Showing _showing;
    private boolean _allowGuesses = false;

    // Count of the colors placed.  Thsi is used to decide whether to display
    // a confirmation dialog when New is clicked.

    private int _placedCount;

    // Count of the colors remaining to be placed.

    private final int[] _colorCounts = new int[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOG) Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        // Get Context-dependent colors resources.

        _emptyCellColor = ContextCompat.getColor(this, R.color.empty_cell);
        _noPuzzleCellColor =
            ContextCompat.getColor(this, R.color.no_puzzle_cell);
        _hintHighlightColor =
            ContextCompat.getColor(this, R.color.hint_highlight);

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

        // Initialize Context-dependent _supplierMap.

        for (Map.Entry<String,PuzzleCreater> entry : _difficultyMap.entrySet()) {
            String difficulty = entry.getKey();
            PuzzleCreater puzzleCreater = entry.getValue();
            
            for (String layoutName : layoutNames) {
                String supplierName = makeSupplierName(difficulty, layoutName);
                PuzzleSupplier puzzleSupplier = new PuzzleSupplier(
                    puzzleCreater,
                    layoutName,
                    log);
                _supplierMap.put(supplierName, puzzleSupplier);
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

        // Initialize the difficulty spinner.

        {
            Spinner spinner = (Spinner) findViewById(R.id.spinner_difficulty);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                new ArrayList(_difficultyMap.keySet()));
            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        // Set up long click on the puzzle/setup button to ask whether
        // to reset the puzzle.

        {
            View buttonSetup = findViewById(R.id.button_setup);
            buttonSetup.setOnLongClickListener((View v) -> {
                Log.i(TAG, "longClick on puzzle");
                askResetPuzzle();
                return true;
            });
        }


        // Set up the touch/gesture listeners for the board circles.

        {
            View boardView = findViewById(R.id.board);

            for (int i = 0; i < 81; i++) {
                final ImageView cellView =
                    (ImageView)boardView.findViewWithTag(Integer.toString(i));

                // Each CellView needs its own
                // GestureDetector/OnGestureListener because the View
                // is not passed in to the listener so it needs to be
                // baked in.

                GestureDetector.OnGestureListener onGestureListener =
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onFling(
                            MotionEvent e1, MotionEvent e2,
                            float velocityX, float velocityY)
                        {
                            unPlace(cellView);
                            return true;
                        }
                    };

                GestureDetector gestureDetector = new GestureDetector(
                    this, onGestureListener);

                cellView.setOnTouchListener((View v, MotionEvent event) ->
                    gestureDetector.onTouchEvent(event));
            }
        }

        _showing = Showing.PLACED;

        {
            // Set up to pop up a verify dialog and/or disable
            // switch_guess when its state changes to true.  This has
            // to use OnCheckedChangeListener and not OnClicklistener
            // because the uder can slide the switch instead of
            // clicking to change the state.

            Switch sw = (Switch) findViewById(R.id.switch_guess);
            sw.setOnCheckedChangeListener(
                (CompoundButton button, boolean isChecked) -> {
                    switchGuessChanged((Switch) button, isChecked);
                });
        }

        // Restore stuff from savedInstanceState.

        if (savedInstanceState != null) {
            if (LOG) Log.i(TAG, "restoring state from Bundle");
            
            PuzzleParcelable puzzleParcelable =
                savedInstanceState.getParcelable(KEY_PUZZLE);
            if (puzzleParcelable != null) {
                if (LOG) Log.i(TAG, "  restoring puzzle from Bundle");
                setPuzzle(puzzleParcelable.getPuzzle());
            }
            else {
                if (LOG) Log.i(TAG, "  Bundle has no puzzle to restore (ok)");
            }
        }

        enableButtons(true);
        enableNewButton();
        setSwitchGuessEnabled();
        hideProgressBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)  {
        super.onPostCreate(savedInstanceState);
        if (LOG) Log.i(TAG, "onPostCreate");
    }

    // Set puzzle as our work-in-progress and set up the UI for it.

    private void setPuzzle(Puzzle puzzle) {
        _puzzle = puzzle;

        _placedCount = (int) Arrays.stream(_puzzle.getCells())
            .filter(c -> !c.isSetup() && c.isPlaced()).count();

        // Set _colorCounts to the number of colors remaining, i.e.,
        // not placed in the puzzle.

        java.util.Arrays.fill(_colorCounts, 9);

        for (Cell cell : _puzzle.getCells()) {
            Integer digit = cell.getPlacedDigit();
            if (digit != null) {
                _colorCounts[digit]--;
            }
        }
    }

    // The puzzle button was long-clicked.  Ask whether to reset the puzzle.

    private void askResetPuzzle() {
        // Don't bother if nothing has been placed yet.

        if (_placedCount == 0) {
            return;
        }

        new AlertDialog.Builder(this)
            .setMessage("Do you want to reset the puzzle and start over?")
            .setNegativeButton("No",
                (DialogInterface dialog, int id) -> {
                    // Just ignore the click.
                })
            .setPositiveButton("Yes, reset",
                (DialogInterface dialog, int id) -> {
                    for (Cell cell : _puzzle.getCells()) {
                        if (!cell.isSetup()) {
                            cell.unplace();
                        }
                    }
                    setPuzzle(_puzzle);
                    showPlaced();
                    showColors();
                    setSwitchGuessEnabled();
                })
            .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (LOG) Log.i(TAG, "onStart");
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (LOG) Log.i(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LOG) Log.i(TAG, "onResume");
        logCircleSize();

        showBoard();
        clearHint();
        maybeCreateBottomRow();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (LOG) Log.i(TAG, "onPostResume");
        logCircleSize();
    }

    private void logCircleSize() {
        View boardView = findViewById(R.id.board);
        ImageView firstCellView =
            (ImageView) boardView.findViewWithTag("0");
        int width = firstCellView.getWidth();
        int height = firstCellView.getHeight();
        if (LOG) Log.i(TAG, "circle is " + width + " x " + height);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (LOG) Log.i(TAG, "onWindowFocusChanged: " + hasFocus);
        if (hasFocus) {
            logCircleSize();
            maybeCreateBottomRow();
        }
    }

    private void maybeCreateBottomRow () {
        // If we haven't yet created the circles in the bottom row, do
        // it now.  But only create it if we're called at a time when
        // the board's view has been layed out do its circles' size is
        // non-zero.  Usually this is when we're called from
        // onWindowFocusChanged, but when switching from multi-window
        // to single window it is when we're called from onResume.
        // XXX Should probably use OnGlobalLayoutListener to do this right:
        // https://stackoverflow.com/questions/8170915/getheight-returns-0-for-all-android-ui-objects

        // Get the size of the first circle on the board.

        View boardView = findViewById(R.id.board);
        ImageView firstCellView =
            (ImageView) boardView.findViewWithTag("0");
        int width = firstCellView.getWidth();
        int height = firstCellView.getHeight();

        // Add the circles tp the bottom_row layout if we haven't
        // already and if the board has been laid out so we get a
        // valid circle width.

        LinearLayout layout = (LinearLayout) findViewById(R.id.bottom_row);

        if (layout.getChildCount() == 0 && width != 0) {
            // Add a circle for each color by inflating a circle
            // layout and setting its size to match he circles on the
            // board.

            for (int i = 0; i < _colors.length; i++) {
                ImageView iv = (ImageView)
                    getLayoutInflater().inflate(R.layout.circles, null);
                LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(width, height);
                iv.setLayoutParams(layoutParams);
                layout.addView(iv);
            }

            layout.requestLayout();

            // Populate _colorViews;

            for (int i = 0; i < _colorViews.length; i++) {
                _colorViews[i] = layout.getChildAt(i);
            }

            // Set the circle colors and visibility.

            for (int i = 0; i < _colorViews.length; i++) {
                ImageView view = (ImageView) _colorViews[i];
                setCircleColor(view, _colors[i]);
                view.setVisibility(_colorCounts[i] == 0 ?
                    View.INVISIBLE : View.VISIBLE);
            }

            // Set up drag stuff.

            for (int cellNumber = 0; cellNumber < 81; cellNumber++) {
                View cellView = boardView.findViewWithTag(
                    Integer.toString(cellNumber));
                cellView.setOnDragListener(
                    new CircleOnDragListener(cellNumber));
            }

            for (int i = 0; i < _colorViews.length; i++) {
                View view = _colorViews[i];

                final int digit = i;

                // Use a touch listener instead of a long click listener
                // to make the drag shadow appear immediately.

                view.setOnTouchListener((View v, MotionEvent event) -> {
                    if (LOG) Log.i(TAG, "view: " + v + " digit: " + digit);
                    if (havePuzzle() &&
                        _showing == Showing.PLACED &&
                        event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    {
                        // Just pass the digit via the local state.  It's
                        // more straightforward than passing it as ClipData.
                        v.startDragAndDrop(
                            null, // ClipDate
                            new CircleDragShadowBuilder(
                                (ImageView)v, _colors[digit]),
                            new Integer(digit),
                            0); // flags
                        return true;
                    }
                    else {
                        return false;
                    }
                });
            }
        }
    }

    // Called before onStop, either before or after onPause.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (LOG) Log.i(TAG, "onSaveInstanceState");
        if (_puzzle != null) {
            if (LOG) Log.i(TAG, "saving state to bundle");
            outState.putParcelable(KEY_PUZZLE, new PuzzleParcelable(_puzzle));
        }
    }

    @Override
    protected void onPause () {
        super.onPause();
        if (LOG) Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (LOG) Log.i(TAG, "onStop");
    }

    // Called betweeb onStop and onStart.
    @Override
    protected void onRestart () {
        super.onRestart();
        if (LOG) Log.i(TAG, "onRestart");
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (LOG) Log.i(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed () {
        if (LOG) Log.i(TAG, "onBackPressed");

        if (!havePuzzle()) {
            // There is no puzzle to worry about losing so just bail.
            super.onBackPressed();
            return;
        }

        String appName = getResources().getString(R.string.app_name);

        new AlertDialog.Builder(this)
            .setMessage("Do you want to exit " + appName + "?\n" +
                "This puzzle will be lost forever.")
            .setNegativeButton("Definitely not",
                (DialogInterface dialog, int id) -> {
                    /* Just ignore the back press. */
                })
            .setPositiveButton("Yes, exit",
                (DialogInterface dialog, int id) ->
                    MainActivity.super.onBackPressed())
            .show();
    }

    private void showBoard () {
        View boardView = findViewById(R.id.board);
        for (int i = 0; i < 81; i++) {
            ImageView cellView =
                (ImageView)boardView.findViewWithTag(Integer.toString(i));
            if (_puzzle != null) {
                showCell(cellView, _puzzle.getCell(i));
            }
            else {
                // The circles never initialize to the correct color.
                // They are either all gray or, especially when the
                // app is exited and onDestroy is called then the app
                // is restarted within about five seconds, they are
                // all the same color llike red, blue, purple, etc.
                // So if we don't have a puzzle then explicitly use
                // _noPuzzleCellColor.
                setCircleColor(cellView, _noPuzzleCellColor);
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
        setCircleColor(cellView, color);

        // Find the sibling TextView and make it visible if this cell
        // is a guess.  And set its text to a somewhat contrasting
        // color.

        ViewGroup parent = (ViewGroup) cellView.getParent();
        TextView tv = (TextView) parent.findViewWithTag("guess");
        boolean showGuess = _showing == Showing.PLACED && cell.isGuess();
        if (showGuess) {
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(0xFF000000 | _guessColors[cell.getPlacedDigit()]);
        }
        else {
            tv.setVisibility(View.INVISIBLE);
        }
    }

    private static void setCircleColor(ImageView cellView, int color) {
        GradientDrawable drawable = (GradientDrawable)cellView.getDrawable();
        setCircleColor(drawable, color);
    }

    private static void setCircleColor(GradientDrawable drawable, int color) {
        drawable.setColor(0xFF000000 | color);
    }

    // place is called when a color clircle is drag/dropped onto this
    // board circle.

    private void place(View cellView, int digit) {
        String tag = (String)cellView.getTag();
        if (LOG) Log.i(TAG, "place " + digit + " in " + tag);

        Cell cell = getCellForCellView(cellView);

        cell.place(digit);
        showCell((ImageView)cellView, cell);

        _colorCounts[digit]--;
        if (_colorCounts[digit] == 0) {
            _colorViews[digit].setVisibility(View.INVISIBLE);
        }

        clearHint();

        _placedCount++;
        setSwitchGuessEnabled();

        if (areAllCellsPlaced()) {
            if (isPuzzleSolved()) {
                showPuzzleIsSolvedMessage();
            }
            else {
                showPuzzleIsWrongMessage();
            }
        }
    }

    // unPlace is called when we detect a fling gesture on a board
    // cell view.

    private void unPlace(View cellView) {
        String tag = (String)cellView.getTag();
        if (LOG) Log.i(TAG, "unPlace " + tag);

        Cell cell = getCellForCellView(cellView);

        if (!cell.isSetup() && cell.isPlaced()) {
            int digit = cell.getPlacedDigit();

            cell.unplace();
            showCell((ImageView)cellView, cell);

            _colorCounts[digit]++;
            if (_colorCounts[digit] == 1) {
                _colorViews[digit].setVisibility(View.VISIBLE);
            }


            clearHint();

            _placedCount--;
            setSwitchGuessEnabled();
        }
    }

    private void enableButtons(boolean enabled) {
        int[] buttonIds = {
            R.id.button_setup,
            R.id.button_solved,
            R.id.button_hint,
        };
        for (int buttonId : buttonIds) {
            View view = findViewById(buttonId);
            if (buttonId == R.id.button_new) {
                view.setEnabled(enabled);
            }
            else {
                view.setEnabled(enabled && havePuzzle());
            }
        }
    }

    private void enableNewButton() {
        Button button = (Button) findViewById(R.id.button_new);
        button.setText("New");
        button.setEnabled(true);
        button.setOnClickListener((View v) -> newWasClicked());
    }

    private void newWasClicked() {
        if (_placedCount == 0) {
            // Nothing is placed, no partially solved puzzle to
            // abandon, just go ahead and make a new one.
            createNewPuzzle();
            return;
        }

        // There is a partially completed puzzle.  Put up a confirm
        // dialog.
            
        new AlertDialog.Builder(this)
            .setMessage("Do you want to abandon this puzzle and create" +
                " a new one?")
            .setNegativeButton("No, keep this one",
                (DialogInterface dialog, int id) -> {/* Ignore this press. */})
            .setPositiveButton("Yes, create a new one",
                (DialogInterface dialog, int id) -> createNewPuzzle())
            .show();
    }

    private void enableNewButtonAfterDelay() {
        Button button = (Button) findViewById(R.id.button_new);
        button.setEnabled(false);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] unused) {
                try {
                    Thread.sleep(2000L);
                }
                catch (InterruptedException ex) {
                    throw new RuntimeException("Shouldn't happen:", ex);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void unused) {
                enableNewButton();
            }
        }.execute();
    }

    private void enableCancelButton(final AsyncCreater.Handle handle) {
        final Button button = (Button) findViewById(R.id.button_new);
        button.setText("Cancel");
        button.setEnabled(true);
        button.setOnClickListener((View v) -> {
            if (LOG) Log.i(TAG, "canceling");
            handle.cancel();
            button.setEnabled(false);
        });
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

    private void showProgressBar() {
        View progressBar =findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        View progressBar =findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
    }

    private boolean havePuzzle() {
        return _puzzle != null;
    }

    // The new button was clicked, or "Try Again" from the
    // timeout dialog.

    private void createNewPuzzle() {
        if (LOG) Log.i(TAG, "createNewPuzzle");

        // Retrieve the selected layout from the layout spinner, and get
        // the corresponding puzzleSupplier.

        String layoutName = getSpinnerItem(R.id.spinner_layout);
        String difficulty = getSpinnerItem(R.id.spinner_difficulty);
        PuzzleSupplier puzzleSupplier =
            _supplierMap.get(makeSupplierName(difficulty, layoutName));

        // Set up a callback for when we have a Puzzle, and get a
        // Handle to cancel it if we need to.

        // But first make a Runnable/lambda to run when the user
        // chooses to give up or presses the back button when we time
        // out.

        Runnable giveUp = () -> {
            enableButtons(true);
            enableNewButton();
            hideProgressBar();
        };

        final AsyncCreater.Handle handle = AsyncCreater.<RawPuzzle>create(
            new WithTimeout(puzzleSupplier, 30000L),

            (RawPuzzle rawPuzzle) -> {
                setPuzzle(toPuzzle(rawPuzzle));
                showPlaced();
                showColors();
                enableButtons(true);
                enableNewButtonAfterDelay();
                setSwitchGuessEnabled();
                hideProgressBar();
            },

            // When the cancel button is clicked the supplier is
            // interrupted and wraps up and finishes (by throwing an
            // Exception), then this is called.
            (Void v) -> giveUp.run(),

            // Called on timeout.
            (Void v) -> {
                new AlertDialog.Builder(this)
                    .setMessage("Puzzle creation is taking a long time.")
                    .setNegativeButton("Keep going",
                        (DialogInterface dialog, int id) -> createNewPuzzle())
                    .setPositiveButton("Give up",
                        (DialogInterface dialog, int id) -> giveUp.run())
                    .setOnCancelListener(
                        (DialogInterface dialog) -> giveUp.run())
                    .show();
            });

        // Disable the buttons until we have a puzzle.  They are
        // re-enabled in the callback.

        enableButtons(false);

        // Enable the cancel button until we get a callback, or the
        // cancel button is pressed.

        enableCancelButton(handle);

        showProgressBar();
    }

    private String getSpinnerItem(int viewId) {
        Spinner spinner = (Spinner) findViewById(viewId);
        return (String) spinner.getSelectedItem();
    }

    private static String makeSupplierName(String difficulty, String layoutName) {
        return difficulty + "-" + layoutName;
    }

    // The setup button was clicked.  Show the setup colors.

    public void clickSetup(View view) {
        if (LOG) Log.i(TAG, "clickSetup");

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
        if (LOG) Log.i(TAG, "clickSolved");

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

    private void showColors() {
        for (int i = 0; i < _colorViews.length; i++) {
            _colorViews[i].setVisibility(
                _colorCounts[i] != 0 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void clickHint(View view) {
        if (LOG) Log.i(TAG, "clickHint");

        if (isPuzzleSolved()) {
            showHint("The puzzle is solved!");
            return;
        }

        int seed = (int) System.currentTimeMillis();
        String puzzleString = getPuzzleString();
        Hint hint = Hinter.getHint(seed, puzzleString);

        showPlaced();
        clearHint();

        if (hint != null) {
            switch (hint.tjpe) {
              case EasyPeasy:
                showHint(
                    "Look for an easy peasy " + _colorNames[hint.digit - 1] +
                    ".");
                break;
              case MissingOne:
                showHint("One space is empty here.");
                break;
              case MissingTwo:
                showHint("Two spaces are empty here.");
                break;
              case Needed:
                showHint("A missing color can go only one place.");
                break;
              case Forced:
                showHint("Only one color is possible here.");
                break;
              case Tricky:
                showHint(
                    "There is a tricky " + _colorNames[hint.digit - 1] + ".");
                break;
              case Guess:
                showHint("I can only guess here.");
                break;
              default:
                showHint(hint.tjpe.toString() + " (XXX).");
                break;
            }
            // If we highlight the row/column for easypeasy then it's
            // too easy, so don't.
            if (hint.tjpe != Heuristic.EasyPeasy) {
                View boardView = findViewById(R.id.board);
                for (int cell : hint.cells) {
                    ImageView cellView = (ImageView)
                        boardView.findViewWithTag(Integer.toString(cell));
                    cellView.setBackgroundColor(
                        0xFF000000 | _hintHighlightColor);
                }
            }
        }
        else {
            showHint("Look for a problem somewhere.");
        }
    }

    private String getPuzzleString() {
        StringBuilder sb = new StringBuilder(81);
        for (Cell cell : _puzzle.getCells()) {
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

    // Called when a board circle is clicked.

    public void toggleGuess(View cellView) {
        String tag = (String)cellView.getTag();
        if (LOG) Log.i(TAG, "clickBoardCircle " + tag);

        if (_allowGuesses) {
            Cell cell = getCellForCellView(cellView);
            if (!cell.isSetup() && cell.isPlaced()) {
                cell.toggleGuess();
                showCell((ImageView)cellView, cell);
            }
        }
    }

    // Create a Puzzle from a RawPuzzle.  This is the only code that
    // knows about both classes.

    private static Puzzle toPuzzle(RawPuzzle rawPuzzle) {
        return new Puzzle(rawPuzzle.puzzle, rawPuzzle.solution);
    }

    private Cell getCellForCellView(View cellView) {
        String tag = (String)cellView.getTag();
        int n = Integer.parseInt(tag);
        return _puzzle.getCell(n);
    }

    private boolean areAllCellsPlaced() {
        return Arrays.stream(_puzzle.getCells()).allMatch(Cell::isPlaced);
    }

    private boolean isPuzzleSolved() {
        return Arrays.stream(_puzzle.getCells()).allMatch(cell ->
            cell.getSolvedDigit().equals(cell.getPlacedDigit()));
    }

    private void showPuzzleIsSolvedMessage() {
        new AlertDialog.Builder(this)
            .setMessage("You got it!")
            .setPositiveButton("Yes, I know",
                (DialogInterface dialog, int id) -> {})
            .show();
    }

    private void showPuzzleIsWrongMessage() {
        new AlertDialog.Builder(this)
            .setMessage("Oops, that's not it.  Keep going.")
            .setPositiveButton("Oh no",
                (DialogInterface dialog, int id) -> {})
            .show();
    }

    // Initially guessing is not allowed.  Guessing can be enabled at
    // any time.  But once it is enabled it can only be changed if
    // nothing has been placed.

    private boolean _ignoreSwitchGuessChanged = false;

    private void switchGuessChanged(Switch sw, boolean isChecked) {
        if (_ignoreSwitchGuessChanged) {
            return;
        }

        if (_placedCount == 0) {
            // Puzzle is reset, just use whatever the switch is now set to.
            _allowGuesses = isChecked;
            return;
        }
        
        // If this is called when _placedCount != 0 then it must be to
        // enable guessing otherwise this button would be disabled.
        // Verify with the user.

        // The switch will be toggled on already.  Turn it back off
        // before we put up the dialog and only turn it on if the user
        // verifies.  XXX Sadly the user may see it briefly turned on.
        // It's probably not easy to stop that.

        _ignoreSwitchGuessChanged = true;
        sw.setChecked(false);
        _ignoreSwitchGuessChanged = false;

        new AlertDialog.Builder(this)
            .setMessage("Allow guessing?  Guessing will not be able" +
                " to be turned off again unless the puzzle is reset.")
            .setPositiveButton("Yes",
                (DialogInterface dialog, int id) -> {
                    _allowGuesses = true;
                    _ignoreSwitchGuessChanged = true;
                    sw.setChecked(_allowGuesses);
                    _ignoreSwitchGuessChanged = false;
                    setSwitchGuessEnabled();
            })
            .setNegativeButton("No",
                (DialogInterface dialog, int id) -> {
                    // Nothing to do.
                })
            .show();
    }

    private void setSwitchGuessEnabled() {
        Switch sw = (Switch) findViewById(R.id.switch_guess);
        sw.setEnabled(!_allowGuesses || _placedCount == 0);
    }

    private static class CircleDragShadowBuilder
        extends View.DragShadowBuilder
    {
        private final int width;
        private final int height;
        private final Drawable drawable;

        public CircleDragShadowBuilder(ImageView v, int color) {
            super(v);
            width = v.getWidth() * 2;
            height = v.getHeight() * 2;
            drawable = v.getDrawable().getConstantState().newDrawable();
            // Make the Drawable scale up to the size we want.
            drawable.setBounds(0, 0, width, height);
            setCircleColor((GradientDrawable)drawable, color);
            if (LOG) Log.i(TAG, "width: " + width + " height: " + height);
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            if (LOG) Log.i(TAG, "onProvideShadowMetrics");
            size.set(width, height);
            touch.set(width / 2, height * 5 / 8);
        }

        // Defines a callback that draws the drag shadow in a Canvas that
        // the system constructs from the dimensions passed in
        // onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {
            if (LOG) Log.i(TAG, "onDrawShadow");
            drawable.draw(canvas);
        }
    }

    private class CircleOnDragListener implements View.OnDragListener {
        private final int cellNumber;

        public CircleOnDragListener(int cellNumber) {
            this.cellNumber = cellNumber;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
              case DragEvent.ACTION_DRAG_STARTED: {
                  // Accept drags in this Activity, if this cell is
                  // not placed, and it's ok to drop the color/digit
                  // in this cell.
                  int digit = (Integer)event.getLocalState();
                  Cell cell = _puzzle.getCell(cellNumber);
                  return !cell.isPlaced() &&
                      (_allowGuesses || digit == cell.getSolvedDigit());
              }
              case DragEvent.ACTION_DRAG_ENTERED:
              case DragEvent.ACTION_DRAG_EXITED:
                return true;    // Ignored.
              case DragEvent.ACTION_DROP: {
                  int digit = (Integer)event.getLocalState();
                  if (LOG) Log.i(TAG, digit + " dropped onto " + cellNumber);
                  place(v, digit);
                  return true;    // Success, not that it matters.
              }
              case DragEvent.ACTION_DRAG_ENDED:
                return true;
              default:
                return false;   // Ignored or useless.
            }
        }
    }

    // PuzzleParcelable and CellParcelable are wrappers that make
    // Puzzle and cell Parcelable so we can put the Puzzle in a Bundle
    // for onSaveInstanceState.  What we really want to put in the
    // Bundle is just a CellParcelable[] but it can only be put in a
    // Parcel not a Bundle.  So PuzzleParcelable is just a container
    // for the CellParcellable[] we really want in the Bundle.

    private static class PuzzleParcelable implements Parcelable {
        private final Puzzle _puzzle;

        PuzzleParcelable(Puzzle puzzle) {
            _puzzle = puzzle;
        }

        Puzzle getPuzzle() {
            return _puzzle;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<PuzzleParcelable> CREATOR =
            new Parcelable.Creator<PuzzleParcelable>() {
                public PuzzleParcelable createFromParcel(Parcel in) {
                    return new PuzzleParcelable(readFromParcel(in));
                }

                @Override
                public PuzzleParcelable[] newArray(int size) {
                    return new PuzzleParcelable[size];
                }
            };
     
        @Override
        public void writeToParcel(Parcel out, int flags) {
            CellParcelable[] cellParcelables =
                Arrays.stream(_puzzle.getCells())
                .map(CellParcelable::new)
                .toArray(CellParcelable[]::new);
            out.writeTypedArray(cellParcelables, 0);
        }

        private static Puzzle readFromParcel(Parcel in) {
            CellParcelable[] cellParcelables =
                in.createTypedArray(CellParcelable.CREATOR);
            Cell[] cells =
                Arrays.stream(cellParcelables)
                .map(CellParcelable::getCell)
                .toArray(Cell[]::new);
            return new Puzzle(cells);
        }
    }

    private static class CellParcelable implements Parcelable {
        private final Cell _cell;

        CellParcelable (Cell cell) {
            _cell = cell;
        }

        Cell getCell() {
            return _cell;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<CellParcelable> CREATOR =
            new Parcelable.Creator<CellParcelable>() {
                public CellParcelable createFromParcel(Parcel in) {
                    return new CellParcelable(readFromParcel(in));
                }

                @Override
                public CellParcelable[] newArray(int size) {
                    return new CellParcelable[size];
                }
            };
     
        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(_cell.getSolvedDigit());
            writeBoolean(out, _cell.isSetup());
            writeBoolean(out, _cell.isPlaced());
            if (_cell.isPlaced()) {
                out.writeInt(_cell.getPlacedDigit());
                writeBoolean(out, _cell.isGuess());
            }
        }

        private static Cell readFromParcel(Parcel in) {
            int digit = in.readInt();
            boolean isSetup = readBoolean(in);
            Cell cell = new Cell(digit, isSetup);

            boolean isPlaced = readBoolean(in);
            if (isPlaced) {
                int placedDigit = in.readInt();
                boolean isGuess = readBoolean(in);
                cell.place(placedDigit);
                cell.setGuess(isGuess);
            }

            return cell;
        }

        private static void writeBoolean(Parcel out, boolean b) {
            out.writeInt(b ? 1 : 0);
        }

        private static boolean readBoolean(Parcel in) {
            return in.readInt() == 1;
        }
    }
}

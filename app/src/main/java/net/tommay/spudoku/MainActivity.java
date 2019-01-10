package net.tommay.spudoku;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.LightingColorFilter;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import net.tommay.spudoku.TimeoutDialogFragment;
import net.tommay.util.Callback;
import net.tommay.util.WithTimeout;

import net.tommay.spudoku.Hinter;

public class MainActivity
    extends AppCompatActivity
    implements TimeoutDialogFragment.Listener
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

    private RawPuzzle _rawPuzzle = null;
    private Puzzle _puzzle = null;
    private Showing _showing;

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

        Resources res = getResources();
        _emptyCellColor = res.getColor(R.color.empty_cell);
        _noPuzzleCellColor = res.getColor(R.color.no_puzzle_cell);
        _hintHighlightColor = res.getColor(R.color.hint_highlight);

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

        _showing = Showing.PLACED;

        // Restore stuff from savedInstanceState.

        if (savedInstanceState != null) {
            String puzzle = savedInstanceState.getString(KEY_PUZZLE);
            String solution = savedInstanceState.getString(KEY_SOLUTION);
            if (puzzle != null && solution != null) {
                if (LOG) Log.i(TAG, "restoring from bundle");
                setPuzzle(new RawPuzzle(puzzle, solution));
            }
        }

        enableButtons(true);
        enableNewButton();
        hideProgressBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)  {
        super.onPostCreate(savedInstanceState);
        if (LOG) Log.i(TAG, "onPostCreate");
    }

    private void setPuzzle(RawPuzzle rawPuzzle) {
        _rawPuzzle = rawPuzzle;
        _puzzle = newPuzzle(_rawPuzzle);

        _placedCount = 0;

        // Set _colorCounts to the number of colors remaining, i.e.,
        // not placed in the puzzle.

        java.util.Arrays.fill(_colorCounts, 9);

        for (int i = 0; i < 81; i++) {
            Cell cell = _puzzle.getCell(i);
            Integer digit = cell.getPlacedDigit();
            if (digit != null) {
                _colorCounts[digit]--;
            }
        }
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
        if (LOG) Log.i(TAG, "onWindowFocusChanged");
        logCircleSize();
        maybeCreateBottomRow();
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

            // Set the circle colors.

            for (int i = 0; i < _colorViews.length; i++) {
                ImageView view = (ImageView) _colorViews[i];
                setCircleColor(view, _colors[i]);
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
                        // Just pass the digit via the local state.
                        // XXX This method was deprecated in API
                        // level 24.  Use startDragAndDrop() for
                        // newer platform versions.
                        v.startDrag(
                            null, // data
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
        if (_rawPuzzle != null) {
            if (LOG) Log.i(TAG, "saving state to bundle");
            outState.putString(KEY_PUZZLE, _rawPuzzle.puzzle);
            outState.putString(KEY_SOLUTION, _rawPuzzle.solution);
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

    public void place(View cellView) {
        String tag = (String)cellView.getTag();
        if (LOG) Log.i(TAG, "place " + tag);

        int n = Integer.parseInt(tag);
        Cell cell = _puzzle.getCell(n);

        cell.setPlaced();
        showCell((ImageView)cellView, cell);

        int digit = cell.getPlacedDigit();

        _colorCounts[digit]--;
        if (_colorCounts[digit] == 0) {
            _colorViews[digit].setVisibility(View.INVISIBLE);
        }

        clearHint();

        _placedCount++;
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
        return _rawPuzzle != null;
    }

    // The new button was clicked, or "Try Again" from the
    // TimeoutDialogFragment.

    public void createNewPuzzle() {
        if (LOG) Log.i(TAG, "new");

        // Retrieve the selected layout from the layout spinner, and get
        // the corresponding puzzleSupplier.

        String layoutName = getSpinnerItem(R.id.spinner_layout);
        String difficulty = getSpinnerItem(R.id.spinner_difficulty);
        PuzzleSupplier puzzleSupplier =
            _supplierMap.get(makeSupplierName(difficulty, layoutName));

        // Set up a callback for when we have a Puzzle, and get a
        // Handle to cancel it if we need to.

        final AsyncCreater.Handle handle = AsyncCreater.<RawPuzzle>create(
            new WithTimeout(puzzleSupplier, 30000L),

            (RawPuzzle rawPuzzle) -> {
                setPuzzle(rawPuzzle);
                showPlaced();
                showColors();
                enableButtons(true);
                enableNewButtonAfterDelay();
                hideProgressBar();
            },

            // When the cancel button is clicked the supplier is
            // interrupted and wraps up and finishes (by throwing an
            // Exception), then this is called.
            (Void v) -> {
                enableButtons(true);
                enableNewButton();
                hideProgressBar();
            },

            // Called on timeout.
            (Void v) -> {
                // The dialog calls actions on this Activity via
                // the TimeoutDialogFragment.Listener interfacee.
                new TimeoutDialogFragment().show(
                    getSupportFragmentManager(),
                    "TimeoutDialogFragment");
            });

        // Disable the buttons until we have a puzzle.  They are
        // re-enabled in the callback.

        enableButtons(false);

        // Enable the cancel button until we get a callback, or the
        // cancel button is pressed.

        enableCancelButton(handle);

        showProgressBar();
    }

    // interface TimeoutDialogFragment.Listener

    @Override
    public void keepGoing() {
        createNewPuzzle();
    }

    @Override
    public void giveUp() {
        enableButtons(true);
        enableNewButton();
        hideProgressBar();
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
        if (LOG) Log.i(TAG, "setup");

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
        if (LOG) Log.i(TAG, "solved");

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
                showHint("One color is missing here.");
                break;
              case MissingTwo:
                showHint("Two colors are missing here.");
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
                        0xFF008000 | _hintHighlightColor);
                }
            }
        }
        else {
            showHint("The puzzle is solved!");
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
              case DragEvent.ACTION_DRAG_STARTED:
                // Accept all drags in this Activity, if this cell is
                // not placed and the correct digit/color is being dragged.
                int digit = (Integer)event.getLocalState();
                Cell cell = _puzzle.getCell(cellNumber);
                return !cell.isPlaced() && digit == cell.getSolvedDigit();
              case DragEvent.ACTION_DRAG_ENTERED:
              case DragEvent.ACTION_DRAG_EXITED:
                return true;    // Ignored.
              case DragEvent.ACTION_DROP:
                if (LOG) Log.i(TAG, cellNumber + " dropped onto");
                place(v);
                return true;    // Success, not that it matters.
              case DragEvent.ACTION_DRAG_ENDED:
                return true;
              default:
                return false;   // Ignored or useless.
            }
        }
    }
}

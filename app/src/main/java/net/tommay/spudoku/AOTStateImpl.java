package net.tommay.spudoku;

import android.content.SharedPreferences;

import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.AOTState;

/**
 * Persists the state of an AOTProducer in a SharedPReferences.
 * Eventually this should be done with sqlite.  When puzzles have
 * difficulty ratings, we can create a puzzle with a given layout,
 * rate it, put it with the rating, and repeat until we get the
 * right rating.
 * This scheme doesn't delete puzzles until a new one is inserted.
 * Actually that may not make sense.  What's the used of allowing N
 * puzzles of a given rating if we always return the first one?
 * get:
 *   fetch one row
 *   if no row then return nul
 *   update row set gotten = true
 *   return a RawPuzzle
 * set:
 *   delete where rating = ? and gotten = true
 *   select count(*) where rating = ?
 *   if count < some limit then insert
 */
class AOTStateImpl implements AOTState<RawPuzzle> {
    private static final String KEY_PUZZLE = "puzzle";
    private static final String KEY_SOLUTION = "solution";

    private final SharedPreferences _preferences;

    public static AOTState create(
        SharedPreferences preferences, RawPuzzle initial)
    {
        AOTState aotState = new AOTStateImpl(preferences);
        if (aotState.get() == null) {
            aotState.put(initial);
        }
        return aotState;
    }

    private AOTStateImpl (SharedPreferences preferences) {
        _preferences = preferences;
    }

    @Override
    public RawPuzzle get () {
        String puzzle = _preferences.getString(KEY_PUZZLE, null);
        String solution = _preferences.getString(KEY_SOLUTION, null);
        if (puzzle != null && solution != null) {
            return new RawPuzzle(puzzle, solution);
        }
        else {
            return null;
        }
    }

    @Override
    public void put (RawPuzzle rawPuzzle) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString(KEY_PUZZLE, rawPuzzle.puzzle);
        editor.putString(KEY_SOLUTION, rawPuzzle.solution);
        editor.commit();
    }
}

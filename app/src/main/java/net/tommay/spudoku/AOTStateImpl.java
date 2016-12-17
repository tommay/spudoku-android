package net.tommay.spudoku;

import android.content.SharedPreferences;

import net.tommay.spudoku.RawPuzzle;
import net.tommay.util.AOTState;

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

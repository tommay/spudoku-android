package net.tommay.spudoku;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Spinner;

public class PersistentSpinner
    extends Spinner
{
    private static final boolean LOG = net.tommay.spudoku.Log.LOG;
    private static final String TAG = net.tommay.spudoku.Log.TAG;

    private final String _name;

/*
    public PersistentSpinner (Context context) {
        super(context);
        if (LOG) Log.i(TAG, "PersistentSpinner(Context)");
    }
*/

    public PersistentSpinner (Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.PersistentSpinner,
            0, 0);

        try {
            _name = a.getString(R.styleable.PersistentSpinner_name);
            // Sadly there is no way to enforce required xml
            // attributes so they can be caught at build time.
            if (_name == null) {
                throw new RuntimeException(
                    "PersistentSpinner requires an xml name attribute");
            }
            Log.i(TAG, "name: " + _name);
        }
        finally {
            a.recycle();
        }
    }
}

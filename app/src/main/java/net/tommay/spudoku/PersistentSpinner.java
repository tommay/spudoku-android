package net.tommay.spudoku;

/* Ideally this could be moved to net.tommay.util.PersistentSpinner.
   But custom attriutes with declare-styleable always end up
   associated with the app's package (from the manifes?) and this
   class shouldn't know or care about the app's packagge.  I.e.,
   attributes end up in net.tommay.spudoku.R, and trying to use them
   with
     <net.tommay.util.PersistentSpinner
       xmlns:util="http://schemas.android.com/apk/res/net.tommay.util"
       util:name="whatever"/>
   or
     <net.tommay.util.PersistentSpinner
       xmlns:util="http://schemas.android.com/apk/res/res-auto"
       util:name="whatever"/>
   fails at compile time because the attributes aren't associated the
   class in net.tommay.util.

   So this seemingly has to live in net.tommay.spudoku.  */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import net.tommay.util.Attributes;

/**
 * A Spinner that uses SharedPreferences to persist the selection
 * across runs.
 */
public class PersistentSpinner
    extends Spinner
{
    private static final boolean LOG = net.tommay.spudoku.Log.LOG;
    private static final String TAG = net.tommay.spudoku.Log.TAG;

    private final String _name;
    private final SharedPreferences _sharedPreferences;

    // We use our own OnItemSelectedListener to persist the selected
    // item when it is selected, so keep a copy of the real listener
    // here and forward events to it.

    private AdapterView.OnItemSelectedListener _onItemSelectedListener = null;

    public PersistentSpinner (Context context, AttributeSet attrs) {
        super(context, attrs);

        _name = Attributes.getAttribute(
            context, attrs,
            R.styleable.PersistentSpinner,
            R.styleable.PersistentSpinner_name);

        // Sadly there is no way to enforce required xml
        // attributes so they can be caught at build time.
        if (_name == null) {
            throw new RuntimeException(
                "PersistentSpinner requires an xml name attribute");
        }

        _sharedPreferences = context.getSharedPreferences(
            this.getClass().getName(), Context.MODE_PRIVATE);

        // Call the superclass method because we want to set the actual
        // listener, not the listener we forward to.

        super.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id)
                {
                    if (_onItemSelectedListener != null) {
                        _onItemSelectedListener.onItemSelected(
                            parent, view, position, id);
                    }
                    String selected = (String) getSelectedItem();
                    _sharedPreferences
                        .edit()
                        .putString(_name, selected)
                        .apply();
                    if (LOG) Log.i(TAG,
                        "saved " + selected + " to preferences for " + _name);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    throw new RuntimeException(
                        "Unexpected call to onNothingSelected");
                }
            });
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        super.setAdapter(adapter);

        // Now that we know the items to select from, select the item
        // that was saved to preferences.

        String selected = _sharedPreferences.getString(_name, null);
        if (LOG) Log.i(TAG, "Selected is " + selected + " for " + _name);
        for (int i = 0, n = getCount(); i < n; i++) {
            String item = (String) getItemAtPosition(i);
            if (LOG) Log.i(TAG, "Item " + i + ": " + item);
            if (item.equals(selected)) {
                if (LOG) Log.i(TAG, "found selected at position " + i);
                setSelection(i);
            }
        }
    }

    // Save the OnItemSelectedListener to forward to.
    //
    @Override
    public void setOnItemSelectedListener(
        AdapterView.OnItemSelectedListener listener)
    {
        _onItemSelectedListener = listener;
    }
}

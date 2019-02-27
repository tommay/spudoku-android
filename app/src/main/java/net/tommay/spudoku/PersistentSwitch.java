package net.tommay.spudoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import net.tommay.util.Attributes;

/**
 * A Switch that uses SharedPreferences to persist the state across
 * runs.
 */
public class PersistentSwitch
    extends Switch
{
    private static final boolean LOG = net.tommay.spudoku.Log.LOG;
    private static final String TAG = net.tommay.spudoku.Log.TAG;

    private final SharedPreferences _sharedPreferences;
    private final String _name;

    // We use our own OnCheckedChangeListener to persist the checked
    // state when it has changed, so keep a copy of the real listener
    // here and forward events to it.

    private CompoundButton.OnCheckedChangeListener _onCheckedChangeListener =
        null;

    public PersistentSwitch (Context context, AttributeSet attrs) {
        super(context, attrs);

        _name = Attributes.getAttribute(
            context, attrs,
            R.styleable.PersistentSwitch,
            R.styleable.PersistentSwitch_name);

        // Sadly there is no way to enforce required xml
        // attributes so they can be caught at build time.
        if (_name == null) {
            throw new RuntimeException(
                "PersistentSwitch requires an xml name attribute");
        }

        _sharedPreferences = context.getSharedPreferences(
            this.getClass().getName(), Context.MODE_PRIVATE);



        if (_sharedPreferences.contains(_name)) {
            setChecked(_sharedPreferences.getBoolean(_name, false));
        }

        // Call the superclass method because we want to set the
        // actual listener, not the listener we forward to.

        super.setOnCheckedChangeListener(
            (CompoundButton buttonView, boolean isChecked) -> {
                if (_onCheckedChangeListener != null) {
                    _onCheckedChangeListener.onCheckedChanged(
                        buttonView, isChecked);
                }

                _sharedPreferences
                    .edit()
                    .putBoolean(_name, isChecked)
                    .apply();

                Log.i(TAG, "saved " + isChecked + " to preferences for " +
                    _name);
            });
    }

    // Save the OnCheckedChangeListener to forward to.
    //
    @Override
    public void setOnCheckedChangeListener(
        CompoundButton.OnCheckedChangeListener listener)
    {
        _onCheckedChangeListener = listener;
    }
}

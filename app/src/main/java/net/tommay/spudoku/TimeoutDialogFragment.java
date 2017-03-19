package net.tommay.spudoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * From the Fragment documentation:
 * Every fragment must have an empty constructor, so it can be
 * instantiated when restoring its activity's state. It is
 * strongly recommended that subclasses do not have other
 * constructors with parameters, since these constructors will not
 * be called when the fragment is re-instantiated; instead,
 * arguments can be supplied by the caller with
 * setArguments(Bundle) and later retrieved by the Fragment with
 * getArguments().
 *
 * http://developer.android.com/reference/android/app/Fragment.html#Fragment()
 */
public class TimeoutDialogFragment extends DialogFragment {
    interface Listener {
        public void keepGoing();
        public void giveUp();
    }

    private Listener _listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder =
            new AlertDialog.Builder(getActivity());
        builder
            .setMessage("Puzzle creation is taking a long time.")
            .setNegativeButton("Keep going",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        _listener.keepGoing();
                    }
                })
            .setPositiveButton("Give up",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        _listener.giveUp();
                    }
                });
        return builder.create();
    }

    // Override the Fragment.onAttach() method to set _listener.

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _listener = (Listener) activity;
    }

    // onCancel is called when the dialog is dismissed without using
    // its own buttons, e.g., with the back button or touching outside
    // the dialog.  Treat this just like "Give up" so we get the
    // correct buttons enabed on the main screen.

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        _listener.giveUp();
    }
}

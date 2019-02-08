package net.tommay.spudoku;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

// Auto-generated from aoo/build.gradle settings.
import net.tommay.spudoku.BuildConfig;

public class HelpActivity
    extends AppCompatActivity
{
    private static final boolean LOG = net.tommay.spudoku.Log.LOG;
    private static final String TAG = net.tommay.spudoku.Log.TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOG) Log.i(TAG, "HelpActivity#onCreate");

        setContentView(R.layout.activity_help);
    }
}

package net.tommay.spudoku;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        ViewGroup vg = findViewById(R.id.help_text);

        for (int i = 0, n = vg.getChildCount(); i < n; i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView)child;
                String text = tv.getText().toString();
                tv.setText(fromHtml(text));
            }
        }


/*
        TextView tv = (TextView) findViewById(R.id.help_view);

        // LinkMovementMethod is a subclass of ScrollingMovementMethod
        // that makes the TextView scrollable *and* makes links work.
        // Weird.

        tv.setMovementMethod(LinkMovementMethod.getInstance());

        tv.setText(fromHtml(getString(R.string.help)));
*/
    }

    public void onClickOk(View cellView) {
        finish();
    }

    // XXX Available as androidx.core.text.HtmlCompat.fromHtml().
    //
    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(source);
        }
    }
}

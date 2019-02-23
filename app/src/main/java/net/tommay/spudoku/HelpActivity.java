package net.tommay.spudoku;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
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

        TextView tv = (TextView) findViewById(R.id.help_view);

        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        tv.setText(fromHtml(getString(R.string.help)));
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

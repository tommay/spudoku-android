package net.tommay.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class Attributes {
    // No instantiation.

    private Attributes() {}

    public static String getAttribute(
        Context context, AttributeSet attributeSet,
        int[] attrs, int idAttribute)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attributeSet,
            attrs,
            0, 0);

        try {
            return a.getString(idAttribute);
        }
        finally {
            a.recycle();
        }
    }
}

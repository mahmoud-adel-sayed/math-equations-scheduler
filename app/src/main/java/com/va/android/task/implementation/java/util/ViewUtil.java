package com.va.android.task.implementation.java.util;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar.Duration;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public final class ViewUtil {
    private ViewUtil() { }

    public static void showSnackBar(@NonNull View root,
                                    @StringRes int message,
                                    @Nullable String actionLabel,
                                    @Duration int duration,
                                    @Nullable View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(root, message, duration);
        if (actionLabel != null && listener != null) {
            snackbar.setAction(actionLabel, listener);
        }
        snackbar.show();
        View view = snackbar.getView();
        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) tv.setGravity(Gravity.CENTER_HORIZONTAL);
    }
}

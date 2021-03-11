package com.va.android.task.implementation.java.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar.Duration;
import com.google.android.material.snackbar.Snackbar;
import com.va.android.task.BuildConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public final class ViewUtil {
    private ViewUtil() { }

    public static void showSnackBar(@NonNull View root, @StringRes int message, @Duration int duration) {
        showSnackBar(root, message, null, duration, null);
    }

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

    /**
     * Tries to parse the content of the editText and throws {@link NumberFormatException} if the
     * content of the editText is empty or not a double value.
     *
     * @param editText The editText.
     * @throws NumberFormatException if the content in the editText is empty or not parsable.
     * @return The content of the editText as double.
     */
    public static double getOperand(@NonNull EditText editText) {
        String operandText = editText.getText().toString();
        if (TextUtils.isEmpty(operandText)) {
            throw new NumberFormatException("operand cannot be empty!");
        }
        return Double.parseDouble(operandText);
    }

    /**
     * Returns an intent that could be used to open the app details in the settings.
     *
     * @return The app settings intent.
     */
    public static Intent getAppSettingsIntent() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}

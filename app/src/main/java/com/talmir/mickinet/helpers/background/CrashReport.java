package com.talmir.mickinet.helpers.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

/**
 * Reporting crashes are depend on value of the
 * SwitchPreference in {@see res/xml/pref_general} file.
 *
 * @author miri
 * @since 7/21/2017
 */
public final class CrashReport {
    public static void report(@NonNull Context context, @Nullable String message) {
        report(context, message, null);
    }

    public static void report(@NonNull Context context, @Nullable Exception e) {
        report(context,null, e);
    }

    public static void report(@NonNull Context context, @Nullable String message, @Nullable Exception e) {
        SharedPreferences sendReportSetting = PreferenceManager.getDefaultSharedPreferences(context);
        if (sendReportSetting.getBoolean("auto_submit_crashes", true))
            Crashlytics.log(message);
    }
}

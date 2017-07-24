package com.talmir.mickinet.helpers.background;

import com.google.firebase.crash.FirebaseCrash;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Reporting crashes are depend on value of the
 * SwitchPreference in {@see res/xml/pref_general} file.
 *
 * @author miri
 * @since 7/21/2017
 */
public final class ReportCrash {
    public static void report(@NonNull Context context, @Nullable String className) {
        report(context, className, null);
    }

    public static void report(@NonNull Context context, @Nullable Exception e) {
        report(context,null, e);
    }

    public static void report(@NonNull Context context, @Nullable String className, @Nullable Exception e) {
        SharedPreferences sendReportSetting = PreferenceManager.getDefaultSharedPreferences(context);
        if (sendReportSetting.getBoolean("auto_submit_crashes", true)) {
            FirebaseCrash.log(className);
            FirebaseCrash.report(e);
        }
    }
}

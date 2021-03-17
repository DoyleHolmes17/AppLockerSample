package com.farid.applockersample;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class Utils {
    private static final String PREFERENCES_FILE = "farid";

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        return ctx.getSharedPreferences(PREFERENCES_FILE, 0).getString(settingName, defaultValue);
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        Editor editor = ctx.getSharedPreferences(PREFERENCES_FILE, 0).edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }
}

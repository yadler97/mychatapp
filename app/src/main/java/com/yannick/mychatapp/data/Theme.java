package com.yannick.mychatapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public enum Theme {
    LIGHT,
    DARK;

    private static final String settingsThemeKey = "settingsTheme";

    public static Theme getByPosition(int position) {
        if (position == 1) {
            return DARK;
        }
        return LIGHT;
    }

    public static Theme getCurrentTheme(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return Theme.valueOf(sharedPref.getString(settingsThemeKey, Theme.LIGHT.toString()));
    }

    public static void setTheme(Context context, Theme theme) {
        if (theme != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(settingsThemeKey, theme.toString());
            editor.apply();
        }
    }
}

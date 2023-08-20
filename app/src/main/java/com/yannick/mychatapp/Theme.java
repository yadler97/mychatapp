package com.yannick.mychatapp;

import android.content.Context;

enum Theme {
    LIGHT,
    DARK;

    public static Theme getByPosition(int position) {
        if (position == 1) {
            return DARK;
        }
        return LIGHT;
    }

    public static Theme getCurrentTheme(Context context) {
        FileOperations fileOperations = new FileOperations(context);
        String fileValue = fileOperations.readFromFile("mychatapp_theme.txt");
        if (!fileValue.isEmpty()) {
            return Theme.valueOf(fileValue);
        }

        return Theme.LIGHT;
    }
}

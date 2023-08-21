package com.yannick.mychatapp.data;

import android.content.Context;

import com.yannick.mychatapp.FileOperations;

public enum Theme {
    LIGHT,
    DARK;

    public static final String fileName = "mychatapp_theme.txt";

    public static Theme getByPosition(int position) {
        if (position == 1) {
            return DARK;
        }
        return LIGHT;
    }

    public static Theme getCurrentTheme(Context context) {
        FileOperations fileOperations = new FileOperations(context);
        String fileValue = fileOperations.readFromFile(fileName);
        if (!fileValue.isEmpty()) {
            return Theme.valueOf(fileValue);
        }

        return Theme.LIGHT;
    }
}

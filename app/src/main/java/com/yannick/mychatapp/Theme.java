package com.yannick.mychatapp;

enum Theme {
    LIGHT,
    DARK;

    public static Theme getByPosition(int position) {
        if (position == 1) {
            return DARK;
        }
        return LIGHT;
    }
}

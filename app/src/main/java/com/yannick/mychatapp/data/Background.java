package com.yannick.mychatapp.data;

import android.content.Context;

import com.yannick.mychatapp.FileOperations;

public enum Background {
    STANDARD,
    BREATH_OF_THE_WILD,
    SPLATOON_2,
    PERSONA_5,
    KIMI_NO_NA_WA,
    SUPER_MARIO_BROS,
    SUPER_MARIO_MAKER,
    XENOBLADE_CHRONICLES_2,
    FIRE_EMBLEM_FATES,
    SUPER_SMASH_BROS_ULTIMATE,
    DETECTIVE_PIKACHU;

    public static final String fileName = "mychatapp_background.txt";

    public static Background getByPosition(int position) {
        switch (position) {
            case 1:
                return BREATH_OF_THE_WILD;
            case 2:
                return SPLATOON_2;
            case 3:
                return PERSONA_5;
            case 4:
                return KIMI_NO_NA_WA;
            case 5:
                return SUPER_MARIO_BROS;
            case 6:
                return SUPER_MARIO_MAKER;
            case 7:
                return XENOBLADE_CHRONICLES_2;
            case 8:
                return FIRE_EMBLEM_FATES;
            case 9:
                return SUPER_SMASH_BROS_ULTIMATE;
            case 10:
                return DETECTIVE_PIKACHU;
            default:
                return STANDARD;
        }
    }

    public static Background getCurrentBackground(Context context) {
        FileOperations fileOperations = new FileOperations(context);
        String fileValue = fileOperations.readFromFile(fileName);
        if (!fileValue.isEmpty()) {
            return Background.valueOf(fileValue);
        }

        return Background.STANDARD;
    }
}

package com.yannick.mychatapp;

enum Background {
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

    public static Background getByPosition(int position) {
        if (position == 1) {
            return BREATH_OF_THE_WILD;
        }
        return STANDARD;
    }
}

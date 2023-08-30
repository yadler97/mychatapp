package com.yannick.mychatapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ApplicationProvider;

import com.yannick.mychatapp.data.Theme;

public class ThemeTest {

    @Test
    public void testGetCurrentTheme() {
        assertEquals(Theme.LIGHT, Theme.getCurrentTheme(ApplicationProvider.getApplicationContext()));
        Theme.setTheme(ApplicationProvider.getApplicationContext(), Theme.DARK);
        assertEquals(Theme.DARK, Theme.getCurrentTheme(ApplicationProvider.getApplicationContext()));
    }
}

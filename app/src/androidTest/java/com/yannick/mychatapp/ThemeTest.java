package com.yannick.mychatapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ApplicationProvider;

public class ThemeTest {

    @Test
    public void testGetCurrentTheme() {
        assertEquals(Theme.LIGHT, Theme.getCurrentTheme(ApplicationProvider.getApplicationContext()));
        FileOperations fileOperations = new FileOperations(ApplicationProvider.getApplicationContext());
        fileOperations.writeToFile("DARK", "mychatapp_theme.txt");
        assertEquals(Theme.DARK, Theme.getCurrentTheme(ApplicationProvider.getApplicationContext()));
    }
}

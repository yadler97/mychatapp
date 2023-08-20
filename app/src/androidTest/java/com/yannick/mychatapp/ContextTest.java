package com.yannick.mychatapp;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

import static org.junit.Assert.*;

public class ContextTest {
    @Test
    public void getAppContext() {
        // Context of the app under test.
        Context appContext = ApplicationProvider.getApplicationContext();

        assertEquals("com.yannick.mychatapp", appContext.getPackageName());
    }
}

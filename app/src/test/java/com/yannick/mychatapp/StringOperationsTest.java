package com.yannick.mychatapp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringOperationsTest {

    @Test
    public void testBuildDate() {
        assertEquals("15.11.2011", StringOperations.convertDateToDisplayFormat("20111115"));
        assertEquals("20111115", StringOperations.convertDateToDatabaseFormat("15.11.2011"));

        assertEquals("15.11.2011", StringOperations.buildDate(2011, 10, 15));
        assertEquals("02.04.2015", StringOperations.buildDate(2015, 3, 2));

        assertEquals(2009, StringOperations.getYear("09.06.2009"));
        assertEquals(5, StringOperations.getMonth("09.06.2009"));
        assertEquals(9, StringOperations.getDay("09.06.2009"));
    }
}

package com.yannick.mychatapp;

public class StringOperations {
    public static String buildDate(int year, int monthOfYear, int dayOfMonth) {
        String date;

        if (dayOfMonth < 10) {
            date = "0" + dayOfMonth;
        } else {
            date = "" + dayOfMonth;
        }

        monthOfYear = monthOfYear + 1;
        if (monthOfYear < 10) {
            date = date + ".0" + monthOfYear + "." + year;
        } else {
            date = date + "." + monthOfYear + "." + year;
        }

        return date;
    }

    public static int getYear(String date) {
        return Integer.parseInt(date.substring(6, 10));
    }

    public static int getMonth(String date) {
        return Integer.parseInt(date.substring(3, 5)) - 1;
    }

    public static int getDay(String date) {
        return Integer.parseInt(date.substring(0, 2));
    }
}

package com.yannick.mychatapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.yannick.mychatapp.data.Message;
import com.yannick.mychatapp.data.Room;

import org.junit.Test;

public class RoomTest {

    @Test
    public void testIsNewer() {
        Room newRoom = new Room("key", "name", 0, "20230101_160000_UTC", "password", "admin");
        Room comparedRoom = new Room("key", "name", 0, "20230101_150000_UTC", "password", "admin");
        assertTrue(newRoom.isNewer(comparedRoom));

        newRoom = new Room("key", "name", 0, "20230101_150000_UTC", "password", "admin");
        Message newestMessage = new Message();
        newestMessage.setTime("20230101_170000_UTC");
        newRoom.setNewestMessage(newestMessage);
        comparedRoom = new Room("key", "name", 0, "20230101_160000_UTC", "password", "admin");
        assertTrue(newRoom.isNewer(comparedRoom));

        newRoom = new Room("key", "name", 0, "20230101_160000_UTC", "password", "admin");
        comparedRoom = new Room("key", "name", 0, "20230101_150000_UTC", "password", "admin");
        newestMessage = new Message();
        newestMessage.setTime("20230101_170000_UTC");
        comparedRoom.setNewestMessage(newestMessage);
        assertFalse(newRoom.isNewer(comparedRoom));

        newRoom = new Room("key", "name", 0, "20230101_150000_UTC", "password", "admin");
        newestMessage = new Message();
        newestMessage.setTime("20230101_180000_UTC");
        newRoom.setNewestMessage(newestMessage);
        comparedRoom = new Room("key", "name", 0, "20230101_160000_UTC", "password", "admin");
        newestMessage = new Message();
        newestMessage.setTime("20230101_170000_UTC");
        comparedRoom.setNewestMessage(newestMessage);
        assertTrue(newRoom.isNewer(comparedRoom));
    }
}

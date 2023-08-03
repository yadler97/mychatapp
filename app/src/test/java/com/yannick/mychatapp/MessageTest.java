package com.yannick.mychatapp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MessageTest {

    @Test
    public void testGetFittingBasicMessageType() {
        assertEquals(Message.Type.MESSAGE_RECEIVED, Message.getFittingBasicMessageType(false, false));
        assertEquals(Message.Type.MESSAGE_RECEIVED_CON, Message.getFittingBasicMessageType(false, true));
        assertEquals(Message.Type.MESSAGE_SENT, Message.getFittingBasicMessageType(true, false));
        assertEquals(Message.Type.MESSAGE_SENT_CON, Message.getFittingBasicMessageType(true, true));
    }
}

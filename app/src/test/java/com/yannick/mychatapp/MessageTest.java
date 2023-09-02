package com.yannick.mychatapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.yannick.mychatapp.data.Message;

import org.junit.Test;

public class MessageTest {

    @Test
    public void testGetFittingBasicMessageType() {
        assertEquals(Message.Type.MESSAGE_RECEIVED, Message.getFittingBasicMessageType(false, false));
        assertEquals(Message.Type.MESSAGE_RECEIVED_CON, Message.getFittingBasicMessageType(false, true));
        assertEquals(Message.Type.MESSAGE_SENT, Message.getFittingBasicMessageType(true, false));
        assertEquals(Message.Type.MESSAGE_SENT_CON, Message.getFittingBasicMessageType(true, true));
    }

    @Test
    public void testIsBasicMessage() {
        assertTrue(Message.isBasicMessage(Message.Type.MESSAGE_RECEIVED));
        assertFalse(Message.isBasicMessage(Message.Type.QUOTE_RECEIVED));
    }

    @Test
    public void testGetQuoteDeletedTypeForQuoteType() {
        assertEquals(Message.Type.QUOTE_RECEIVED_DELETED, Message.getQuoteDeletedTypeForQuoteType(Message.Type.QUOTE_RECEIVED));
        assertEquals(Message.Type.QUOTE_RECEIVED_DELETED_CON, Message.getQuoteDeletedTypeForQuoteType(Message.Type.QUOTE_RECEIVED_CON));
        assertEquals(Message.Type.QUOTE_SENT_DELETED, Message.getQuoteDeletedTypeForQuoteType(Message.Type.QUOTE_SENT));
        assertEquals(Message.Type.QUOTE_SENT_DELETED_CON, Message.getQuoteDeletedTypeForQuoteType(Message.Type.QUOTE_SENT_CON));
    }
}

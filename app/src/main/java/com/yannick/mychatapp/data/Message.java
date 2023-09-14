package com.yannick.mychatapp.data;

public class Message {
    private User user;
    private String text;
    private String time;
    private boolean sender;
    private String key;
    private Type type;
    private Message quotedMessage;
    private String searchString;
    private boolean pinned;

    public Message() {
        this.type = Type.HEADER;
    }

    public Message(User user, String text, String time, String id, Type type, Message quotedMessage, boolean pinned) {
        this.user = user;
        this.text = text;
        this.time = time;
        this.key = id;
        this.type = type;
        this.quotedMessage = quotedMessage;
        this.searchString = "";
        this.pinned = pinned;
    }

    public enum Type {
        HEADER,
        MESSAGE_RECEIVED,
        MESSAGE_RECEIVED_CON,
        MESSAGE_SENT,
        MESSAGE_SENT_CON,
        QUOTE_RECEIVED,
        QUOTE_RECEIVED_CON,
        QUOTE_SENT,
        QUOTE_SENT_CON,
        QUOTE_RECEIVED_DELETED,
        QUOTE_RECEIVED_DELETED_CON,
        QUOTE_SENT_DELETED,
        QUOTE_SENT_DELETED_CON,
        IMAGE_RECEIVED,
        IMAGE_RECEIVED_CON,
        IMAGE_SENT,
        IMAGE_SENT_CON,
        QUOTE_IMAGE_RECEIVED,
        QUOTE_IMAGE_RECEIVED_CON,
        QUOTE_IMAGE_SENT,
        QUOTE_IMAGE_SENT_CON,
        FORWARDED_RECEIVED,
        FORWARDED_RECEIVED_CON,
        FORWARDED_SENT,
        FORWARDED_SENT_CON,
        LINK_PREVIEW_RECEIVED,
        LINK_PREVIEW_RECEIVED_CON,
        LINK_PREVIEW_SENT,
        LINK_PREVIEW_SENT_CON,
        EXPANDABLE_RECEIVED,
        EXPANDABLE_RECEIVED_CON,
        EXPANDABLE_SENT,
        EXPANDABLE_SENT_CON,
        FORWARDED_EXPANDABLE_RECEIVED,
        FORWARDED_EXPANDABLE_RECEIVED_CON,
        FORWARDED_EXPANDABLE_SENT,
        FORWARDED_EXPANDABLE_SENT_CON,
    }

    public static boolean isBasicMessage(Type type) {
        return type == Type.MESSAGE_RECEIVED || type == Type.MESSAGE_RECEIVED_CON || type == Type.MESSAGE_SENT || type == Type.MESSAGE_SENT_CON;
    }

    public static boolean isQuote(Type type) {
        return type == Type.QUOTE_RECEIVED || type == Type.QUOTE_RECEIVED_CON || type == Type.QUOTE_SENT || type == Type.QUOTE_SENT_CON;
    }

    public static boolean isDeletedQuote(Type type) {
        return type == Type.QUOTE_RECEIVED_DELETED || type == Type.QUOTE_RECEIVED_DELETED_CON || type == Type.QUOTE_SENT_DELETED || type == Type.QUOTE_SENT_DELETED_CON;
    }

    public static boolean isImage(Type type) {
        return type == Type.IMAGE_RECEIVED || type == Type.IMAGE_RECEIVED_CON || type == Type.IMAGE_SENT || type == Type.IMAGE_SENT_CON;
    }

    public static boolean isQuoteImage(Type type) {
        return type == Type.QUOTE_IMAGE_RECEIVED || type == Type.QUOTE_IMAGE_RECEIVED_CON || type == Type.QUOTE_IMAGE_SENT || type == Type.QUOTE_IMAGE_SENT_CON;
    }

    public static boolean isForwardedMessage(Type type) {
        return type == Type.FORWARDED_RECEIVED || type == Type.FORWARDED_RECEIVED_CON || type == Type.FORWARDED_SENT || type == Type.FORWARDED_SENT_CON;
    }

    public static boolean isLinkPreview(Type type) {
        return type == Type.LINK_PREVIEW_RECEIVED || type == Type.LINK_PREVIEW_RECEIVED_CON || type == Type.LINK_PREVIEW_SENT || type == Type.LINK_PREVIEW_SENT_CON;
    }

    public static boolean isExpandable(Type type) {
        return type == Type.EXPANDABLE_RECEIVED || type == Type.EXPANDABLE_RECEIVED_CON || type == Type.EXPANDABLE_SENT || type == Type.EXPANDABLE_SENT_CON;
    }

    public static boolean isForwardedExpandable(Type type) {
        return type == Type.FORWARDED_EXPANDABLE_RECEIVED || type == Type.FORWARDED_EXPANDABLE_RECEIVED_CON || type == Type.FORWARDED_EXPANDABLE_SENT || type == Type.FORWARDED_EXPANDABLE_SENT_CON;
    }

    public static boolean isConMessage(Type type) {
        return type == Type.MESSAGE_RECEIVED_CON || type == Type.MESSAGE_SENT_CON
                || type == Type.QUOTE_RECEIVED_CON || type == Type.QUOTE_SENT_CON
                || type == Type.QUOTE_RECEIVED_DELETED_CON || type == Type.QUOTE_SENT_DELETED_CON
                || type == Type.IMAGE_RECEIVED_CON || type == Type.IMAGE_SENT_CON
                || type == Type.QUOTE_IMAGE_RECEIVED_CON || type == Type.QUOTE_IMAGE_SENT_CON
                || type == Type.FORWARDED_RECEIVED_CON || type == Type.FORWARDED_SENT_CON
                || type == Type.LINK_PREVIEW_RECEIVED_CON || type == Type.LINK_PREVIEW_SENT_CON
                || type == Type.EXPANDABLE_RECEIVED_CON || type == Type.EXPANDABLE_SENT_CON
                || type == Type.FORWARDED_EXPANDABLE_RECEIVED_CON || type == Type.FORWARDED_EXPANDABLE_SENT_CON;
    }

    public static Type getFittingBasicMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.MESSAGE_RECEIVED;
            }

            return Type.MESSAGE_RECEIVED_CON;
        }

        if (!con) {
            return Type.MESSAGE_SENT;
        }

        return Type.MESSAGE_SENT_CON;
    }

    public static Type getFittingQuoteMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.QUOTE_RECEIVED;
            }

            return Type.QUOTE_RECEIVED_CON;
        }

        if (!con) {
            return Type.QUOTE_SENT;
        }

        return Type.QUOTE_SENT_CON;
    }

    public static Type getFittingQuoteDeletedMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.QUOTE_RECEIVED_DELETED;
            }

            return Type.QUOTE_RECEIVED_DELETED_CON;
        }

        if (!con) {
            return Type.QUOTE_SENT_DELETED;
        }

        return Type.QUOTE_SENT_DELETED_CON;
    }

    public static Type getFittingImageMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.IMAGE_RECEIVED;
            }

            return Type.IMAGE_RECEIVED_CON;
        }

        if (!con) {
            return Type.IMAGE_SENT;
        }

        return Type.IMAGE_SENT_CON;
    }

    public static Type getFittingQuoteImageMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.QUOTE_IMAGE_RECEIVED;
            }

            return Type.QUOTE_IMAGE_RECEIVED_CON;
        }

        if (!con) {
            return Type.QUOTE_IMAGE_SENT;
        }

        return Type.QUOTE_IMAGE_SENT_CON;
    }

    public static Type getFittingForwardedMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.FORWARDED_RECEIVED;
            }

            return Type.FORWARDED_RECEIVED_CON;
        }

        if (!con) {
            return Type.FORWARDED_SENT;
        }

        return Type.FORWARDED_SENT_CON;
    }

    public static Type getFittingLinkPreviewMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.LINK_PREVIEW_RECEIVED;
            }

            return Type.LINK_PREVIEW_RECEIVED_CON;
        }

        if (!con) {
            return Type.LINK_PREVIEW_SENT;
        }

        return Type.LINK_PREVIEW_SENT_CON;
    }

    public static Type getFittingExpandableMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.EXPANDABLE_RECEIVED;
            }

            return Type.EXPANDABLE_RECEIVED_CON;
        }

        if (!con) {
            return Type.EXPANDABLE_SENT;
        }

        return Type.EXPANDABLE_SENT_CON;
    }

    public static Type getFittingForwardedExpandableMessageType(boolean sender, boolean con) {
        if (!sender) {
            if (!con) {
                return Type.FORWARDED_EXPANDABLE_RECEIVED;
            }

            return Type.FORWARDED_EXPANDABLE_RECEIVED_CON;
        }

        if (!con) {
            return Type.FORWARDED_EXPANDABLE_SENT;
        }

        return Type.FORWARDED_EXPANDABLE_SENT_CON;
    }

    public static Type getQuoteDeletedTypeForQuoteType(Type type) {
        switch (type) {
            case QUOTE_RECEIVED:
                return Type.QUOTE_RECEIVED_DELETED;
            case QUOTE_RECEIVED_CON:
                return Type.QUOTE_RECEIVED_DELETED_CON;
            case QUOTE_SENT:
                return Type.QUOTE_SENT_DELETED;
            case QUOTE_SENT_CON:
                return Type.QUOTE_SENT_DELETED_CON;
            default:
                return Type.HEADER;
        }
    }

    public static Type getNonConTypeForConType(Type type) {
        switch (type) {
            case MESSAGE_RECEIVED_CON:
                return Type.MESSAGE_RECEIVED;
            case MESSAGE_SENT_CON:
                return Type.MESSAGE_SENT;
            case QUOTE_RECEIVED_CON:
                return Type.QUOTE_RECEIVED;
            case QUOTE_SENT_CON:
                return Type.QUOTE_SENT;
            case QUOTE_RECEIVED_DELETED_CON:
                return Type.QUOTE_RECEIVED_DELETED;
            case QUOTE_SENT_DELETED_CON:
                return Type.QUOTE_SENT_DELETED;
            case IMAGE_RECEIVED_CON:
                return Type.IMAGE_RECEIVED;
            case IMAGE_SENT_CON:
                return Type.IMAGE_SENT;
            case QUOTE_IMAGE_RECEIVED_CON:
                return Type.QUOTE_IMAGE_RECEIVED;
            case QUOTE_IMAGE_SENT_CON:
                return Type.QUOTE_IMAGE_SENT;
            case FORWARDED_RECEIVED_CON:
                return Type.FORWARDED_RECEIVED;
            case FORWARDED_SENT_CON:
                return Type.FORWARDED_SENT;
            case LINK_PREVIEW_RECEIVED_CON:
                return Type.LINK_PREVIEW_RECEIVED;
            case LINK_PREVIEW_SENT_CON:
                return Type.LINK_PREVIEW_SENT;
            case EXPANDABLE_RECEIVED_CON:
                return Type.EXPANDABLE_RECEIVED;
            case EXPANDABLE_SENT_CON:
                return Type.EXPANDABLE_SENT;
            case FORWARDED_EXPANDABLE_RECEIVED_CON:
                return Type.FORWARDED_EXPANDABLE_RECEIVED;
            case FORWARDED_EXPANDABLE_SENT_CON:
                return Type.FORWARDED_EXPANDABLE_SENT;
            default:
                return Type.HEADER;
        }
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public boolean isSender(String userID) {
        return userID.equals(this.getUser().getUserID());
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Message getQuotedMessage() {
        return quotedMessage;
    }

    public void setQuotedMessage(Message quotedMessage) {
        this.quotedMessage = quotedMessage;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
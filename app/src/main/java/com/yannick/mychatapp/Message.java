package com.yannick.mychatapp;

public class Message {
    private User user;
    private String msg;
    private String time;
    private String bTime;
    private boolean sender;
    private String key;
    private int typ;
    private String quote_name;
    private String quote_message;
    private String quote_key;
    private String searchString;
    private String pin;

    public Message() {

    }

    public Message(User user, String msg, String time, String bTime, boolean sender, String id, int typ, String quote_name, String quote_message, String quote_key, String pin) {
        this.user = user;
        this.msg = msg;
        this.time = time;
        this.bTime = bTime;
        this.sender = sender;
        this.key = id;
        this.typ = typ;
        this.quote_message = quote_message;
        this.quote_name = quote_name;
        this.quote_key = quote_key;
        this.searchString = "";
        this.pin = pin;
    }

    public String getMsg() {
        return msg;
    }

    public String getTime() {
        return time;
    }

    public boolean isSender() {
        return sender;
    }

    public String getKey() {
        return key;
    }

    public int getTyp() {
        return typ;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public String getQuote_name() {
        return quote_name;
    }

    public void setQuote_name(String quote_name) {
        this.quote_name = quote_name;
    }

    public String getQuote_message() {
        return quote_message;
    }

    public void setQuote_message(String quote_message) {
        this.quote_message = quote_message;
    }

    public String getQuote_key() {
        return quote_key;
    }

    public void setTyp(int typ) {
        this.typ = typ;
    }

    public String getbTime() {
        return bTime;
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

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
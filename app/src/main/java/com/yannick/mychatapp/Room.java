package com.yannick.mychatapp;

public class Room {
    private String key;
    private String name;
    private String admin;
    private String caty;
    private String time;
    private String passwd;
    private String desc;
    private Message nM;
    private String username;
    private String img;

    public Room(String key, String name, String caty, String time, String passwd, String admin) {
        this.key = key;
        this.name = name;
        this.caty = caty;
        this.time = time;
        this.passwd = passwd;
        this.admin = admin;
        this.desc = "";
    }

    public String getName() {
        return name;
    }

    public String getCaty() {
        return caty;
    }

    public String getTime() {
        return time;
    }

    public String getPasswd() {
        return passwd;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDesc() {
        return desc;
    }

    public String getAdmin() {
        return admin;
    }

    public Message getnM() {
        return nM;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setnM(Message nM) {
        this.nM = nM;
    }

    public String getImg() {
        return img;
    }
}
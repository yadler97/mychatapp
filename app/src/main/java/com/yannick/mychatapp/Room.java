package com.yannick.mychatapp;

public class Room {
    private String key;
    private String name;
    private String admin;
    private String category;
    private String time;
    private String passwd;
    private String desc;
    private Message nM;
    private String username;
    private String img;

    public Room() {

    }

    public Room(String key, String name, String category, String time, String passwd, String admin) {
        this.key = key;
        this.name = name;
        this.category = category;
        this.time = time;
        this.passwd = passwd;
        this.admin = admin;
        this.desc = "";
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
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

    public void setDesc(String desc) {
        this.desc = desc;
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
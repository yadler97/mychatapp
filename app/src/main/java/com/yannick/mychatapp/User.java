package com.yannick.mychatapp;

public class User {
    private String userID;
    private String name;
    private String bday;
    private String bio;
    private String loc;
    private String favc;
    private String ownpi;
    private String img;
    private String banner;

    public User() {

    }

    public User(String userid, String name, String bday, String bio, String loc, String favc, String img, String banner) {
        this.userID = userid;
        this.name = name;
        this.bday = bday;
        this.bio = bio;
        this.loc = loc;
        this.favc = favc;
        this.img = img;
        this.banner = banner;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getBday() {
        return bday;
    }

    public String getBio() {
        return bio;
    }

    public String getLoc() {
        return loc;
    }

    public String getFavc() {
        return favc;
    }

    public String getOwnpi() {
        return ownpi;
    }

    public String getImg() {
        return img;
    }

    public String getBanner() {
        return banner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBday(String bday) {
        this.bday = bday;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public void setOwnpi(String ownpi) {
        this.ownpi = ownpi;
    }
}
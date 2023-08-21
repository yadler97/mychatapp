package com.yannick.mychatapp.data;

public class User {
    private String userID;
    private String name;
    private String birthday;
    private String profileDescription;
    private String location;
    private String favColour;
    private String ownpi;
    private String img;
    private String banner;

    public User() {

    }

    public User(String userid, String name, String birthday, String profileDescription, String location, String favColour, String img, String banner) {
        this.userID = userid;
        this.name = name;
        this.birthday = birthday;
        this.profileDescription = profileDescription;
        this.location = location;
        this.favColour = favColour;
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

    public String getBirthday() {
        return birthday;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public String getLocation() {
        return location;
    }

    public String getFavColour() {
        return favColour;
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

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOwnpi(String ownpi) {
        this.ownpi = ownpi;
    }
}
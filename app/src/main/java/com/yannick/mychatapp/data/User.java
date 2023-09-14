package com.yannick.mychatapp.data;

public class User {
    private String userID;
    private String name;
    private String birthday;
    private String description;
    private String location;
    private int favColour;
    private boolean ownProfileImage;
    private String image;
    private String banner;

    public User() {

    }

    public User(String userid, String name, String birthday, String description, String location, int favColour, String image, String banner) {
        this.userID = userid;
        this.name = name;
        this.birthday = birthday;
        this.description = description;
        this.location = location;
        this.favColour = favColour;
        this.image = image;
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

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public int getFavColour() {
        return favColour;
    }

    public boolean getOwnProfileImage() {
        return ownProfileImage;
    }

    public String getImage() {
        return image;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOwnProfileImage(boolean ownProfileImage) {
        this.ownProfileImage = ownProfileImage;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }
}
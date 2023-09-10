package com.yannick.mychatapp.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Room {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_z");
    private String key;
    private String name;
    private String admin;
    private int category;
    private String time;
    private String passwd;
    private String desc;
    private Message newestMessage;
    private String username;
    private String img;
    private boolean muted;
    private String searchString;

    public Room() {
        this.searchString = "";
    }

    public Room(String key, String name, int category, String time, String passwd, String admin) {
        this.key = key;
        this.name = name;
        this.category = category;
        this.time = time;
        this.passwd = passwd;
        this.admin = admin;
        this.desc = "";
        this.searchString = "";
    }

    public String getName() {
        return name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
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

    public Message getNewestMessage() {
        return newestMessage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNewestMessage(Message newestMessage) {
        this.newestMessage = newestMessage;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public boolean isNewer(Room comparedRoom) {
        if (this.getNewestMessage() != null) {
            if (comparedRoom.getNewestMessage() != null) {
                try {
                    return sdf.parse(this.getNewestMessage().getTime()).after(sdf.parse(comparedRoom.getNewestMessage().getTime()));
                } catch (ParseException e) {
                    return false;
                }
            } else {
                try {
                    return sdf.parse(this.getNewestMessage().getTime()).after(sdf.parse(comparedRoom.getTime()));
                } catch (ParseException e) {
                    return false;
                }
            }
        } else {
            if (comparedRoom.getNewestMessage() != null) {
                try {
                    return sdf.parse(this.getTime()).after(sdf.parse(comparedRoom.getNewestMessage().getTime()));
                } catch (ParseException e) {
                    return false;
                }
            } else {
                try {
                    return sdf.parse(this.getTime()).after(sdf.parse(comparedRoom.getTime()));
                } catch (ParseException e) {
                    return false;
                }
            }
        }
    }
}
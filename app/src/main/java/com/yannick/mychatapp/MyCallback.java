package com.yannick.mychatapp;

import com.yannick.mychatapp.data.User;

public interface MyCallback {
    void onCallback(String key, User user, String time, String chat_msg, String img, String pin, String quote);
}

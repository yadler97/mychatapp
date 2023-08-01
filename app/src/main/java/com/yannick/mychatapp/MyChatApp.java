package com.yannick.mychatapp;

import com.google.firebase.database.FirebaseDatabase;

public class MyChatApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

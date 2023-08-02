package com.yannick.mychatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SplashActivity extends AppCompatActivity{
    private final static int SPLASH_OUT_TIME = 2000;
    private ImageView imgsplash;
    private String theme;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        changeTheme();
        setContentView(R.layout.activity_splash);

        imgsplash = findViewById(R.id.imgsplash);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        if (theme.equals("1")) {
            imgsplash.setImageResource(R.drawable.ic_splash_dark);
            params.putString("theme", "dark");
        } else {
            imgsplash.setImageResource(R.drawable.ic_splash);
            params.putString("theme", "light");
        }
        mFirebaseAnalytics.logEvent("theme_type", params);
        createNotificationChannel();

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    Intent homeIntent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                } else {
                    if (user != null) {
                        mAuth.signOut();
                    }
                    Intent homeIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(homeIntent);
                }
                finish();
            }
        }, SPLASH_OUT_TIME);
    }

    private void changeTheme() {
        theme = readFromFile("mychatapp_theme.txt");
        if (theme.equals("1")) {
            setTheme(R.style.SplashDark);
        } else {
            setTheme(R.style.Splash);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("channel_id", "mychatapp", importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.YELLOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription("mychatapp description");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public String readFromFile(String datei) {
        Context context = this;
        String erg = "";

        try {
            InputStream inputStream = context.openFileInput(datei);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                erg = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return erg;
    }
}
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
    //String htmlContentInStringFormat = "";
    //String htmlContentInStringFormat2 = "";
    //String htmlContentInStringFormat3 = "";
    //String htmlContentInStringFormat4 = "";
    //Document htmlDocument;
    //String testURL = "https://derstandard.at/2000099930972/T-Mobile-startet-kommende-Woche-Oesterreichs-erstes-5G-Netz";

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

        //MyTask task = new MyTask();
        //task.execute(testURL);
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

    /*private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                htmlDocument = Jsoup.connect(testURL).userAgent("Googlebot/2.1 (+http://www.google.com/bot.html)").get();
                if (htmlDocument != null) {
                    String url = new URL(htmlDocument.location()).getHost();
                    if (url.contains("www.")) {
                        url = url.substring(4);
                    }
                    htmlContentInStringFormat2 = url;
                    Elements meta3e = htmlDocument.select("meta[property=og:image]");
                    Elements meta2e = htmlDocument.select("meta[property=og:description]");
                    Elements metae = htmlDocument.select("meta[property=og:title]");
                    if (!meta2e.isEmpty() && !metae.isEmpty() && !meta3e.isEmpty()) {
                        Element meta3 = meta3e.first();
                        Element meta2 = meta2e.first();
                        Element meta = metae.first();
                        htmlContentInStringFormat4 = meta3.attr("content");
                        htmlContentInStringFormat3 = meta2.attr("content");
                        htmlContentInStringFormat = meta.attr("content");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (htmlDocument != null && !htmlContentInStringFormat.isEmpty() && !htmlContentInStringFormat2.isEmpty() && !htmlContentInStringFormat3.isEmpty() && !htmlContentInStringFormat4.isEmpty()) {
                Log.d("HEYHY", "Titel: " + htmlContentInStringFormat);
                Log.d("HEYHY", "Seite: " + htmlContentInStringFormat2);
                Log.d("HEYHY", "Teaser: " + htmlContentInStringFormat3);
                Log.d("HEYHY", "Bild: " + htmlContentInStringFormat4);
            }
        }
    }

    private class MyTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con =  (HttpURLConnection) new URL(params[0]).openConnection();
                con.setRequestMethod("HEAD");
                System.out.println(con.getResponseCode());
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean bResponse = result;
            if (bResponse) {
                Log.d("HEYHY", "File exists!");
                JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                jsoupAsyncTask.execute();
            } else {
                Log.d("HEYHY", "File does not exist!");
            }
        }
    }*/
}
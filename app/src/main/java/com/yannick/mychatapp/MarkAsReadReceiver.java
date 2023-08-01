package com.yannick.mychatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MarkAsReadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        updateNotification(context, intent.getIntExtra("push_id", 1));

        writeToFile(intent.getStringExtra("message_id"), "mychatapp_raum_" + intent.getStringExtra("room_key") + "_nm.txt", context);
    }

    private void updateNotification(Context context, int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notifyId);
    }

    public void writeToFile(String text, String datei, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(datei, Context.MODE_PRIVATE));
            outputStreamWriter.write(text);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}

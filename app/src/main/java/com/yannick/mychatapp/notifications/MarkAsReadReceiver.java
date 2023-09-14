package com.yannick.mychatapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;

import com.yannick.mychatapp.FileOperations;

public class MarkAsReadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        updateNotification(context, intent.getIntExtra("push_id", 1));

        FileOperations fileOperations = new FileOperations(context);
        fileOperations.writeToFile(intent.getStringExtra("message_key"), String.format(FileOperations.newestMessageFilePattern, intent.getStringExtra("room_key")));
    }

    private void updateNotification(Context context, int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notifyId);
    }
}

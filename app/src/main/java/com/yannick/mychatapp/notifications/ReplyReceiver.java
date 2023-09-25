package com.yannick.mychatapp.notifications;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yannick.mychatapp.Constants;
import com.yannick.mychatapp.FileOperations;
import com.yannick.mychatapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReplyReceiver extends BroadcastReceiver {

    private CharSequence getReplyMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence("key_text_reply");
        }
        return null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        CharSequence message = getReplyMessage(intent);

        DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child(Constants.roomsDatabaseKey).child(intent.getStringExtra("room_key")).child(Constants.messagesDatabaseKey);
        String tempKey = root.push().getKey();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_z");
        String currentDateAndTime = sdf.format(new Date());

        DatabaseReference messageRoot = root.child(tempKey);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sender", intent.getStringExtra("user_id"));
        map.put("text", message.toString());
        map.put("image", "");
        map.put("pinned", false);
        map.put("forwarded", false);
        map.put("quote", "");
        map.put("time", currentDateAndTime);

        messageRoot.updateChildren(map);

        updateNotification(context, intent.getIntExtra("push_id", 1));

        FileOperations fileOperations = new FileOperations(context);
        fileOperations.writeToFile(tempKey, String.format(FileOperations.newestMessageFilePattern, intent.getStringExtra("room_key")));
    }

    private void updateNotification(Context context, int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.ic_stat_ic_stat_onesignal_default)
                .setContentText(context.getResources().getString(R.string.messagesent));

        notificationManager.notify(notifyId, builder.build());
    }
}

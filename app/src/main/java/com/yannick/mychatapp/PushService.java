package com.yannick.mychatapp;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yannick.mychatapp.activities.ChatActivity;
import com.yannick.mychatapp.activities.MainActivity;

import java.util.List;

import static android.content.ContentValues.TAG;

public class PushService extends FirebaseMessagingService {

    private static String KEY_TEXT_REPLY = "key_text_reply";
    private FirebaseAuth mAuth;

    public PushService() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mAuth = FirebaseAuth.getInstance();
        FileOperations fileOperations = new FileOperations(this);
        if (!appInForeground(this) || (appInForeground(this) && !fileOperations.readFromFile("mychatapp_current.txt").equals(remoteMessage.getData().get("roomid")))) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            if (settings.getBoolean(MainActivity.settingsPushNotificationsKey, true) && !remoteMessage.getData().get("userid").equals(mAuth.getCurrentUser().getUid())) {
                int pushID = 0;
                for (int i = 0; i < remoteMessage.getData().get("roomid").length(); ++i) {
                    pushID += (int) remoteMessage.getData().get("roomid").charAt(i);
                }
                String pushtext;
                if (!remoteMessage.getData().get("img").isEmpty()) {
                    pushtext = remoteMessage.getData().get("name") + " " + getResources().getString(R.string.sharedapicture);
                } else {
                    pushtext = remoteMessage.getData().get("name") + ": " + remoteMessage.getData().get("msg");
                }

                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("room_name", remoteMessage.getData().get("roomname"));
                intent.putExtra("room_key", remoteMessage.getData().get("roomid"));
                intent.putExtra("user_id", mAuth.getCurrentUser().getUid());
                intent.putExtra("nmid", remoteMessage.getData().get("messageid"));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent contentIntent = PendingIntent.getActivity(this, (int) (Math.random() * 100), intent, PendingIntent.FLAG_IMMUTABLE);

                RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(getResources().getString(R.string.sendmessage)).build();

                NotificationCompat.Action replyAction =
                        new NotificationCompat.Action.Builder(
                                R.drawable.ic_stat_ic_stat_onesignal_default,
                                getResources().getString(R.string.reply), getReplyPendingIntent(remoteMessage.getData().get("roomid"), pushID))
                                .addRemoteInput(remoteInput)
                                .setAllowGeneratedReplies(true)
                                .build();

                NotificationCompat.Action markAsReadAction =
                        new NotificationCompat.Action.Builder(
                                R.drawable.ic_stat_ic_stat_onesignal_default,
                                getResources().getString(R.string.markasread), getMarkAsReadPendingIntent(remoteMessage.getData().get("roomid"), pushID, remoteMessage.getData().get("messageid")))
                                .setAllowGeneratedReplies(true)
                                .build();

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                        .setContentTitle(remoteMessage.getData().get("roomname"))
                        .setContentText(pushtext)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLights(Color.YELLOW, 1000, 1000)
                        .setColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                        .setSmallIcon(R.drawable.ic_stat_ic_stat_onesignal_default)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher))
                        .setContentIntent(contentIntent)
                        .addAction(replyAction)
                        .addAction(markAsReadAction)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(pushID, notificationBuilder.build());

                Log.d(TAG, "From: " + remoteMessage.getFrom());

                if (remoteMessage.getData().size() > 0) {
                    Log.d(TAG, "Message data payload: " + remoteMessage.getData());
                }
            }
        }
    }

    private PendingIntent getReplyPendingIntent(String roomid, int pushid) {
        Intent intent = new Intent(getApplicationContext(), ReplyReceiver.class);
        intent.putExtra("room_key", roomid);
        intent.putExtra("user_id", mAuth.getCurrentUser().getUid());
        intent.putExtra("push_id", pushid);
        return PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getMarkAsReadPendingIntent(String roomid, int pushid, String messageid) {
        Intent intent = new Intent(getApplicationContext(), MarkAsReadReceiver.class);
        intent.putExtra("room_key", roomid);
        intent.putExtra("user_id", mAuth.getCurrentUser().getUid());
        intent.putExtra("push_id", pushid);
        intent.putExtra("message_id", messageid);
        return PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean appInForeground(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (runningAppProcess.processName.equals(context.getPackageName()) &&
                    runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }
}
package com.thalesgroup.d1.templates.core.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.thalesgroup.gemalto.d1.messaging.Message;

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();
    private static final String d1ChannelId = "d1_channel_id_111";
    private static int notificationId = 10001;

    /***
     * Attempts to post a user notification or fails silently.
     * The implementation requires POST_NOTIFICATIONS permission, which is requested by the app at start time.
     * Thus, we do not handle the missing permission here.
     *
     * @param message A D1 message to be displayed (carries title & message)
     */
    @SuppressLint("MissingPermission")
    public static void showUserNotification(final Message message){

        final Context context = ApplicationContextResolver.getApplicationContext();

        // Step 1: Create Notification Channel
        createNotificationChannel(context);

        // Step 2: Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, d1ChannelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon (e.g., R.drawable.ic_notification)
                .setContentTitle(message.getTitle())
                .setContentText(message.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Forces sound
                .setAutoCancel(true);

        // Step 3: Show the Notification
        // Note: For Android 13+ (API 33+), make sure you have requested the POST_NOTIFICATIONS permission at runtime
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(notificationId++, builder.build());
        } else {
            Log.w(TAG, "Unable to post a notification because the permission was not granted!");
        }

    }

    public static void createNotificationChannel(Context context) {

        // The code requires API 26+, but we have minSdkVersion=27

        CharSequence name = "D1 Notification";
        String descriptionText = "Notifications provided by D1 platform";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(d1ChannelId, name, importance);
        channel.setDescription(descriptionText);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}

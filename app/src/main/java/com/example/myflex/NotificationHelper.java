package com.example.myflex;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "myflex_channel";

    // Create the channel
    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "MyFlex Notifications";
            String description = "Therapy reminders and motivation";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    name,
                    importance
            );

            channel.setDescription(description);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channel);
        }
    }

    //Show notification
    public static void showNotification(Context context,
                                        String title,
                                        String message) {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.shape_blob_green)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU
                || androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            notificationManager.notify(
                    (int) System.currentTimeMillis(),
                    builder.build()
            );
        }
    }
}
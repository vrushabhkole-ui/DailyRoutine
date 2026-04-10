package com.example.dailyroutine;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "ROUTINE_ALARM_CHANNEL";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String toneUri = intent.getStringExtra("toneUri");

        // Start AlarmService to play sound
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("toneUri", toneUri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Routine Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null); // Sound handled by AlarmService
            notificationManager.createNotificationChannel(channel);
        }

        // Dismiss intent
        Intent dismissIntent = new Intent(context, DismissReceiver.class);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tasks)
                .setContentTitle("Routine Reminder")
                .setContentText("It's time for: " + title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(R.drawable.ic_tasks, "Dismiss", dismissPendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
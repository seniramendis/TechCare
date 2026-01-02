package com.example.techcare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "techcare_updates";
    public static final String CHANNEL_NAME = "Booking Updates";
    public static final String CHANNEL_DESC = "Notifications for repair booking status";

    // Create the Notification Channel (Required for Android 8.0+)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Trigger a notification
    public static void sendBookingNotification(Context context, String title, String message) {
        // Intent to open MyBookingsActivity when notification is clicked
        Intent intent = new Intent(context, MyBookingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // [FIX] Use the App's Launcher Icon for the "Large Icon" (Colorful image on the right)
        // This ensures the logo is visible and colored, not white-on-white.
        Bitmap appIconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_only_removebg_preview);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                // [FIX] Small Icon MUST be the transparent version (for the status bar)
                .setSmallIcon(R.drawable.logo_only_removebg_preview)
                // [FIX] Set the Large Icon to the app icon
                .setLargeIcon(appIconBitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
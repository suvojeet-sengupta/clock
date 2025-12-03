package com.suvojeet.clock.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.suvojeet.clock.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Clock:AlarmWakeLock")
        wakeLock.acquire(10 * 1000L) // Acquire for 10 seconds

        try {
            val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Alarm"
            val channelId = "alarm_channel_high_priority"
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val channel = NotificationChannel(
                    channelId,
                    "High Priority Alarm Channel",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for Alarm Manager"
                    enableVibration(true)
                    setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val fullScreenIntent = Intent(context, com.suvojeet.clock.ui.alarm.AlarmActivity::class.java).apply {
                putExtra("EXTRA_MESSAGE", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
            val fullScreenPendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Alarm")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false) // Don't auto cancel, wait for user action
                .setOngoing(true)     // Persistent until dismissed
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .build()
                
            notificationManager.notify(999, notification)
            
            // Force start activity if we have the overlay permission, regardless of version
            // This is critical for Android 10+ (Q) and especially Android 14/15
            if (android.provider.Settings.canDrawOverlays(context)) {
                 context.startActivity(fullScreenIntent)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Fallback for very old devices without overlay requirement
                context.startActivity(fullScreenIntent)
            }
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}

package com.dadomatch.shared.feature.engagement

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dadomatch.shared.R

/** Shows one scheduled reminder; tapping it opens the app on Home. */
class EngagementNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission") // checked right before notify()
    override suspend fun doWork(): Result {
        val manager = NotificationManagerCompat.from(applicationContext)
        // Turned off since it was scheduled: drop it silently
        if (!manager.areNotificationsEnabled()) return Result.success()

        val id = inputData.getString(KEY_ID) ?: return Result.success()
        ensureChannel(applicationContext)

        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(applicationContext, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_dadomatch)
            .setContentTitle(inputData.getString(KEY_TITLE))
            .setContentText(inputData.getString(KEY_BODY))
            .setStyle(NotificationCompat.BigTextStyle().bigText(inputData.getString(KEY_BODY)))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(id.hashCode(), notification)
        return Result.success()
    }

    companion object {
        /** Also the FCM default channel (see the manifest), so campaigns land in it too. */
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        const val TAG = "engagement_reminder"
        const val CHANNEL_ID = "reminders"
        const val KEY_ID = "id"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}

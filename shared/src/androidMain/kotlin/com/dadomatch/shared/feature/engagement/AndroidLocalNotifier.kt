package com.dadomatch.shared.feature.engagement

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dadomatch.shared.feature.auth.presentation.CurrentActivityTracker
import com.dadomatch.shared.feature.engagement.domain.LocalNotification
import com.dadomatch.shared.feature.engagement.domain.LocalNotifier
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

/**
 * Reminders as delayed WorkManager jobs: they survive reboots and app kills, and
 * don't need the exact-alarm permission (a few minutes of drift is fine here).
 */
class AndroidLocalNotifier(
    private val context: Context,
    private val activityTracker: CurrentActivityTracker,
) : LocalNotifier {

    init {
        // Created up front: FCM campaigns may arrive before any reminder has fired
        EngagementNotificationWorker.ensureChannel(context)
    }

    override suspend fun isPermissionGranted(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    override suspend fun requestPermission(): Boolean {
        if (isPermissionGranted()) return true
        // Below Android 13 notifications are on by default and there is no runtime prompt
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val activity = activityTracker.activity ?: return false
        // The answer arrives after the dialog closes; the app re-plans on resume,
        // which picks the new state up
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
        return false
    }

    override suspend fun replaceAll(notifications: List<LocalNotification>) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(EngagementNotificationWorker.TAG)
        val now = Clock.System.now()
        notifications.forEach { notification ->
            val request = OneTimeWorkRequestBuilder<EngagementNotificationWorker>()
                .setInitialDelay((notification.fireAt - now).inWholeMilliseconds.coerceAtLeast(0), TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        EngagementNotificationWorker.KEY_ID to notification.id,
                        EngagementNotificationWorker.KEY_TITLE to notification.title,
                        EngagementNotificationWorker.KEY_BODY to notification.body,
                    )
                )
                .addTag(EngagementNotificationWorker.TAG)
                .build()
            workManager.enqueue(request)
        }
    }

    private companion object {
        const val REQUEST_CODE = 4711
    }
}

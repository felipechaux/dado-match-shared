package com.dadomatch.shared.feature.engagement.domain

import kotlin.time.Instant

/** One reminder ready to hand to the OS. */
data class LocalNotification(
    val id: String,
    val title: String,
    val body: String,
    val fireAt: Instant,
)

/** Platform scheduler for on-device reminders (WorkManager on Android, UNUserNotificationCenter on iOS). */
interface LocalNotifier {
    suspend fun isPermissionGranted(): Boolean

    /** Shows the system permission prompt (no-op where none is needed). Returns whether it is granted. */
    suspend fun requestPermission(): Boolean

    /** Replaces every reminder previously scheduled by this app with [notifications]. */
    suspend fun replaceAll(notifications: List<LocalNotification>)
}

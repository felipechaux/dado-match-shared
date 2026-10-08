package com.dadomatch.shared.feature.engagement

import com.dadomatch.shared.feature.engagement.domain.LocalNotification
import com.dadomatch.shared.feature.engagement.domain.LocalNotifier
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Clock
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

class IosLocalNotifier : LocalNotifier {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun isPermissionGranted(): Boolean = suspendCancellableCoroutine { cont ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            cont.resume(status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional)
        }
    }

    // The APNs registration FCM needs is done by the app delegate on every launch
    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { cont ->
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ -> cont.resume(granted) }
    }

    override suspend fun replaceAll(notifications: List<LocalNotification>) {
        // Only ours are pending (FCM ones are delivered, never scheduled), so removing all is safe
        center.removeAllPendingNotificationRequests()
        val now = Clock.System.now()
        notifications.forEach { notification ->
            val seconds = (notification.fireAt - now).inWholeSeconds
            if (seconds <= 0) return@forEach
            val content = UNMutableNotificationContent().apply {
                setTitle(notification.title)
                setBody(notification.body)
                setSound(UNNotificationSound.defaultSound())
            }
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds.toDouble(), repeats = false)
            center.addNotificationRequest(
                UNNotificationRequest.requestWithIdentifier(notification.id, content, trigger),
                withCompletionHandler = null,
            )
        }
    }
}

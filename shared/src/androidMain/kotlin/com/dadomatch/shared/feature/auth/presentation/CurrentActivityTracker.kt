package com.dadomatch.shared.feature.auth.presentation

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Remembers the activity currently in the foreground.
 *
 * Credential Manager must be given an Activity to show its account picker: on
 * Android 13 and older it falls back to Play Services, which fails with
 * "Failed to launch the selector UI ... ensure the context parameter is an
 * Activity-based context" when handed the application context Koin provides.
 *
 * Must be created at app start (Koin createdAtStart) so it sees the first
 * activity resume. Held weakly, so it never leaks a destroyed activity.
 */
class CurrentActivityTracker(application: Application) {

    private var current: WeakReference<Activity>? = null

    val activity: Activity?
        get() = current?.get()?.takeUnless { it.isFinishing || it.isDestroyed }

    init {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                current = WeakReference(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (current?.get() === activity) current = null
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }
}

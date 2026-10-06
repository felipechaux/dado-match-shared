package com.dadomatch.shared.presentation.ui.navigation

/**
 * Lets a platform-native tab bar replace the Compose [LiquidFooterMenu].
 *
 * Used on iOS so the system draws the bar with real Liquid Glass (iOS 26), while
 * navigation stays in the shared NavHost: Compose publishes the tab bar state to
 * the [listener] and the native bar reports taps back through [selectTab].
 */
class NativeTabBarBridge {

    /** Set by the native side (Swift) to render the bar. */
    var listener: NativeTabBarListener? = null
        set(value) {
            field = value
            // Replay the latest state so a late listener starts in sync
            value?.onTabTitlesChanged(titles)
            value?.onTabBarStateChanged(selectedIndex, isVisible)
        }

    internal var onTabSelected: ((Int) -> Unit)? = null

    private var titles: List<String> = emptyList()
    private var selectedIndex: Int = NO_TAB_SELECTED
    private var isVisible: Boolean = false

    /** Called by the native bar when the user taps the tab at [index]. */
    fun selectTab(index: Int) {
        onTabSelected?.invoke(index)
    }

    internal fun publishTitles(titles: List<String>) {
        if (titles == this.titles) return
        this.titles = titles
        listener?.onTabTitlesChanged(titles)
    }

    internal fun publishState(selectedIndex: Int, isVisible: Boolean) {
        if (selectedIndex == this.selectedIndex && isVisible == this.isVisible) return
        this.selectedIndex = selectedIndex
        this.isVisible = isVisible
        listener?.onTabBarStateChanged(selectedIndex, isVisible)
    }

    companion object {
        /** [NativeTabBarListener.onTabBarStateChanged] index when no tab is current (e.g. Paywall). */
        const val NO_TAB_SELECTED = -1
    }
}

interface NativeTabBarListener {
    /** Localized tab titles, in tab order. Re-sent when the app language changes. */
    fun onTabTitlesChanged(titles: List<String>)

    /**
     * [selectedIndex] is the current tab, or [NativeTabBarBridge.NO_TAB_SELECTED].
     * [isVisible] is false on screens without a bottom bar (splash, paywall, onboarding).
     */
    fun onTabBarStateChanged(selectedIndex: Int, isVisible: Boolean)
}

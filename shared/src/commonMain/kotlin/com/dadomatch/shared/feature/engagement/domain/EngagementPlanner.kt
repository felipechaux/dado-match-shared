package com.dadomatch.shared.feature.engagement.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** Reminder types, in priority order: when two land on the same day, the first one wins. */
enum class NotificationKind {
    STREAK,
    ROLLS_REFILLED,
    NIGHT_PLAN,
    MISS_YOU,
}

data class PlannedNotification(
    val kind: NotificationKind,
    val fireAt: Instant,
)

/** Everything the planner needs to know about the user, read once per planning pass. */
data class EngagementState(
    val lastOpenedAt: Instant?,
    /** Consecutive days with at least one icebreaker, ending on [lastActiveDay]. */
    val streakDays: Int,
    val lastActiveDay: LocalDate?,
    /** When a free user's spent rolls come back; null when they still have rolls (or are Pro). */
    val rollsRefillAt: Instant?,
    val enabledKinds: Set<NotificationKind>,
)

/**
 * Decides which reminders to schedule for the next few days. Pure — no platform or
 * storage access — so the rules are easy to read and to test.
 *
 * At most one reminder per local day: an app that nags gets uninstalled.
 */
object EngagementPlanner {

    const val HORIZON_DAYS = 8
    const val MISS_YOU_AFTER_DAYS = 3
    private val NIGHT_PLAN_DAYS = setOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    private val NIGHT_PLAN_TIME = LocalTime(20, 0)
    private val STREAK_TIME = LocalTime(19, 0)
    private val MISS_YOU_TIME = LocalTime(18, 30)

    fun plan(state: EngagementState, now: Instant, timeZone: TimeZone): List<PlannedNotification> {
        val today = now.toLocalDateTime(timeZone).date
        val candidates = buildList {
            // Streak at risk: the day after the last active one, if it was a real streak
            val lastActive = state.lastActiveDay
            if (state.streakDays >= 2 && lastActive != null) {
                add(PlannedNotification(NotificationKind.STREAK, lastActive.plus(1, DateTimeUnit.DAY).at(STREAK_TIME, timeZone)))
            }
            state.rollsRefillAt?.let { add(PlannedNotification(NotificationKind.ROLLS_REFILLED, it)) }
            for (offset in 0 until HORIZON_DAYS) {
                val day = today.plus(offset, DateTimeUnit.DAY)
                if (day.dayOfWeek in NIGHT_PLAN_DAYS) {
                    add(PlannedNotification(NotificationKind.NIGHT_PLAN, day.at(NIGHT_PLAN_TIME, timeZone)))
                }
            }
            state.lastOpenedAt?.let { opened ->
                val day = opened.toLocalDateTime(timeZone).date.plus(MISS_YOU_AFTER_DAYS, DateTimeUnit.DAY)
                add(PlannedNotification(NotificationKind.MISS_YOU, day.at(MISS_YOU_TIME, timeZone)))
            }
        }

        val horizonEnd = today.plus(HORIZON_DAYS, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        return candidates
            .filter { it.kind in state.enabledKinds && it.fireAt > now && it.fireAt < horizonEnd }
            .groupBy { it.fireAt.toLocalDateTime(timeZone).date }
            .map { (_, sameDay) -> sameDay.minBy { it.kind.ordinal } }
            .sortedBy { it.fireAt }
    }

    private fun LocalDate.at(time: LocalTime, timeZone: TimeZone): Instant = atTime(time).toInstant(timeZone)

    private fun LocalDate.atStartOfDayIn(timeZone: TimeZone): Instant = atTime(LocalTime(0, 0)).toInstant(timeZone)
}

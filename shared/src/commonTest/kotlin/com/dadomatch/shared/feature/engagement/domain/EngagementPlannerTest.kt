package com.dadomatch.shared.feature.engagement.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class EngagementPlannerTest {

    private val zone = TimeZone.of("America/Bogota")

    // Monday 2026-10-05 10:00 local
    private val now = LocalDateTime(2026, 10, 5, 10, 0).toInstant(zone)

    private fun state(
        lastOpenedAt: LocalDateTime? = LocalDateTime(2026, 10, 5, 9, 0),
        streakDays: Int = 0,
        lastActiveDay: LocalDate? = null,
        rollsRefillAt: LocalDateTime? = null,
        enabledKinds: Set<NotificationKind> = NotificationKind.entries.toSet(),
    ) = EngagementState(
        lastOpenedAt = lastOpenedAt?.toInstant(zone),
        streakDays = streakDays,
        lastActiveDay = lastActiveDay,
        rollsRefillAt = rollsRefillAt?.toInstant(zone),
        enabledKinds = enabledKinds,
    )

    private fun List<PlannedNotification>.local() = map { it.kind to it.fireAt.toLocalDateTime(zone) }

    @Test
    fun nightPlanOnThursdayToSaturdayAt20() {
        val plan = EngagementPlanner.plan(state(enabledKinds = setOf(NotificationKind.NIGHT_PLAN)), now, zone).local()
        assertEquals(
            listOf(
                NotificationKind.NIGHT_PLAN to LocalDateTime(2026, 10, 8, 20, 0),
                NotificationKind.NIGHT_PLAN to LocalDateTime(2026, 10, 9, 20, 0),
                NotificationKind.NIGHT_PLAN to LocalDateTime(2026, 10, 10, 20, 0),
            ),
            plan,
        )
    }

    @Test
    fun missYouThreeDaysAfterLastOpen() {
        val plan = EngagementPlanner.plan(state(enabledKinds = setOf(NotificationKind.MISS_YOU)), now, zone).local()
        assertEquals(listOf(NotificationKind.MISS_YOU to LocalDateTime(2026, 10, 8, 18, 30)), plan)
    }

    @Test
    fun atMostOnePerDayAndHigherPriorityWins() {
        // Thursday 10-08 has night plan (20:00) and miss you (18:30): night plan outranks it
        val plan = EngagementPlanner.plan(state(), now, zone)
        val perDay = plan.groupBy { it.fireAt.toLocalDateTime(zone).date }
        assertTrue(perDay.values.all { it.size == 1 })
        assertEquals(NotificationKind.NIGHT_PLAN, perDay.getValue(LocalDate(2026, 10, 8)).single().kind)
    }

    @Test
    fun streakReminderTheDayAfterLastActive() {
        val plan = EngagementPlanner.plan(
            state(streakDays = 3, lastActiveDay = LocalDate(2026, 10, 5), enabledKinds = setOf(NotificationKind.STREAK)),
            now,
            zone,
        ).local()
        assertEquals(listOf(NotificationKind.STREAK to LocalDateTime(2026, 10, 6, 19, 0)), plan)
    }

    @Test
    fun noStreakReminderForASingleDay() {
        val plan = EngagementPlanner.plan(
            state(streakDays = 1, lastActiveDay = LocalDate(2026, 10, 5), enabledKinds = setOf(NotificationKind.STREAK)),
            now,
            zone,
        )
        assertTrue(plan.isEmpty())
    }

    @Test
    fun rollsRefilledWhenTheyComeBack() {
        val refill = LocalDateTime(2026, 10, 6, 8, 15)
        val plan = EngagementPlanner.plan(
            state(rollsRefillAt = refill, enabledKinds = setOf(NotificationKind.ROLLS_REFILLED)),
            now,
            zone,
        ).local()
        assertEquals(listOf(NotificationKind.ROLLS_REFILLED to refill), plan)
    }

    @Test
    fun pastAndDisabledRemindersAreDropped() {
        val plan = EngagementPlanner.plan(
            state(
                lastOpenedAt = LocalDateTime(2026, 9, 1, 9, 0), // miss-you date long gone
                rollsRefillAt = LocalDateTime(2026, 10, 5, 8, 0), // already refilled
                enabledKinds = setOf(NotificationKind.MISS_YOU, NotificationKind.ROLLS_REFILLED),
            ),
            now,
            zone,
        )
        assertTrue(plan.isEmpty())
    }
}

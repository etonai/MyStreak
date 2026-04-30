package com.mystreak.app.util

import com.mystreak.app.data.model.TaskActivity

object StreakCalculator {
    fun calculate(activities: List<TaskActivity>): Int {
        if (activities.isEmpty()) return 0

        val today = DateUtils.todayEpochDay()
        val activeDays = activities
            .map { DateUtils.timestampToEpochDay(it.timestamp) }
            .toSortedSet()

        // Streak must include today or yesterday to be active
        if (!activeDays.contains(today) && !activeDays.contains(today - 1)) return 0

        var streak = 0
        var checkDay = if (activeDays.contains(today)) today else today - 1

        while (activeDays.contains(checkDay)) {
            streak++
            checkDay--
        }
        return streak
    }
}

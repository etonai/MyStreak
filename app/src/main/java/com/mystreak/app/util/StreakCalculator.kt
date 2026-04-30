package com.mystreak.app.util

object StreakCalculator {
    fun calculate(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val today = DateUtils.todayEpochDay()
        val activeDays = timestamps
            .map { DateUtils.timestampToEpochDay(it) }
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

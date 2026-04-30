package com.mystreak.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_day_cache")
data class CalendarDayCache(
    @PrimaryKey val dateEpochDay: Long,
    val colorLevel: CalendarColorLevel
)

enum class CalendarColorLevel { NONE, LIGHT_BLUE, MEDIUM_BLUE, DARK_BLUE, BRIGHT_GREEN }

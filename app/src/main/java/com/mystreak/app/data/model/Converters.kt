package com.mystreak.app.data.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromTaskPriority(value: TaskPriority): String = value.name
    @TypeConverter fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter fun fromSuccessLevel(value: SuccessLevel): String = value.name
    @TypeConverter fun toSuccessLevel(value: String): SuccessLevel = SuccessLevel.valueOf(value)

    @TypeConverter fun fromCalendarColorLevel(value: CalendarColorLevel): Int = value.ordinal
    @TypeConverter fun toCalendarColorLevel(value: Int): CalendarColorLevel = CalendarColorLevel.entries[value]
}

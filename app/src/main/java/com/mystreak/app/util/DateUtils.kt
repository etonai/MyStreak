package com.mystreak.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val zone get() = ZoneId.systemDefault()

    fun todayEpochDay(): Long = LocalDate.now(zone).toEpochDay()

    fun timestampToEpochDay(timestamp: Long): Long =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate().toEpochDay()

    fun dayBounds(epochDay: Long): Pair<Long, Long> {
        val date = LocalDate.ofEpochDay(epochDay)
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    fun tomorrowStart(): Long {
        val (_, end) = dayBounds(todayEpochDay())
        return end
    }

    fun rollingWindowStart(days: Int): Long {
        val startDay = LocalDate.now(zone).minusDays(days.toLong() - 1)
        return startDay.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun formatTime(timestamp: Long): String {
        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone)
        return ldt.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    fun formatDateTime(timestamp: Long): String {
        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone)
        return ldt.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val date = LocalDate.of(year, month, 1)
        return date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    fun localDateToTimestamp(date: LocalDate, hour: Int = 12, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
}

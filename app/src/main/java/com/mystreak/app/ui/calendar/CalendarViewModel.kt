package com.mystreak.app.ui.calendar

import androidx.lifecycle.*
import com.mystreak.app.data.model.CalendarColorLevel
import com.mystreak.app.data.model.CalendarDayCache
import com.mystreak.app.data.repository.MyStreakRepository
import com.mystreak.app.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarCell(
    val epochDay: Long,           // -1 for empty padding cells
    val dayOfMonth: Int,          // 0 for empty
    val colorLevel: CalendarColorLevel,
    val isToday: Boolean
)

class CalendarViewModel(private val repo: MyStreakRepository) : ViewModel() {

    private val _displayYear = MutableLiveData(LocalDate.now().year)
    private val _displayMonth = MutableLiveData(LocalDate.now().monthValue)

    val monthLabel: LiveData<String> = _displayYear.switchMap { year ->
        _displayMonth.map { month -> DateUtils.formatMonthYear(year, month) }
    }

    val calendarCells: LiveData<List<CalendarCell>> =
        repo.getAllCachedDays()
            .combine(_displayYear.asFlow()) { cache, year -> Pair(cache, year) }
            .combine(_displayMonth.asFlow()) { (cache, year), month -> Triple(cache, year, month) }
            .map { (cache, year, month) -> buildCells(cache, year, month) }
            .asLiveData()

    private fun buildCells(
        cache: List<CalendarDayCache>,
        year: Int,
        month: Int
    ): List<CalendarCell> {
        val cacheMap = cache.associateBy { it.dateEpochDay }
        val today = DateUtils.todayEpochDay()
        val firstOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = firstOfMonth.lengthOfMonth()
        // Sunday=0..Saturday=6 offset
        val startOffset = (firstOfMonth.dayOfWeek.value % 7)

        val cells = mutableListOf<CalendarCell>()
        repeat(startOffset) {
            cells.add(CalendarCell(-1L, 0, CalendarColorLevel.NONE, false))
        }
        for (day in 1..daysInMonth) {
            val date = LocalDate.of(year, month, day)
            val epochDay = date.toEpochDay()
            val isToday = epochDay == today
            val color = when {
                isToday -> CalendarColorLevel.NONE // live color loaded separately
                else -> cacheMap[epochDay]?.colorLevel ?: CalendarColorLevel.NONE
            }
            cells.add(CalendarCell(epochDay, day, color, isToday))
        }
        return cells
    }

    fun goToPreviousMonth() {
        val y = _displayYear.value ?: return
        val m = _displayMonth.value ?: return
        val prev = LocalDate.of(y, m, 1).minusMonths(1)
        _displayYear.value = prev.year
        _displayMonth.value = prev.monthValue
    }

    fun goToNextMonth() {
        val y = _displayYear.value ?: return
        val m = _displayMonth.value ?: return
        val next = LocalDate.of(y, m, 1).plusMonths(1)
        _displayYear.value = next.year
        _displayMonth.value = next.monthValue
    }

    fun refreshTodayColor(onColor: (CalendarColorLevel) -> Unit) {
        viewModelScope.launch {
            onColor(repo.computeLiveTodayColor())
        }
    }

    companion object {
        fun factory(repo: MyStreakRepository) = viewModelFactory {
            initializer { CalendarViewModel(repo) }
        }
    }
}

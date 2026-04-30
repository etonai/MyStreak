package com.mystreak.app.ui.dashboard

import androidx.lifecycle.*
import com.mystreak.app.data.model.ActivityWithTask
import com.mystreak.app.data.model.Task
import com.mystreak.app.data.repository.MyStreakRepository
import com.mystreak.app.util.DateUtils
import com.mystreak.app.util.StreakCalculator
import kotlinx.coroutines.launch

class DashboardViewModel(private val repo: MyStreakRepository) : ViewModel() {

    val todayActivities: LiveData<List<ActivityWithTask>> =
        repo.getActivitiesForDay(DateUtils.todayEpochDay()).asLiveData()

    val yesterdayActivities: LiveData<List<ActivityWithTask>> =
        repo.getActivitiesForDay(DateUtils.todayEpochDay() - 1).asLiveData()

    private val _streak = MutableLiveData(0)
    val streak: LiveData<Int> = _streak

    private val _weekSummary = MutableLiveData(Pair(0, 0))
    val weekSummary: LiveData<Pair<Int, Int>> = _weekSummary

    private val _outstandingTasks = MutableLiveData<List<Task>>(emptyList())
    val outstandingTasks: LiveData<List<Task>> = _outstandingTasks

    init {
        viewModelScope.launch {
            repo.getActivitiesForDay(DateUtils.todayEpochDay()).collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _streak.value = StreakCalculator.calculate(repo.getAllActivitiesOnce())
            _weekSummary.value = repo.getWeekSummaryCounts()
            _outstandingTasks.value = repo.getActiveHighPriorityOutstandingTasks()
        }
    }

    companion object {
        fun factory(repo: MyStreakRepository) = viewModelFactory {
            initializer { DashboardViewModel(repo) }
        }
    }
}

package com.mystreak.app.ui.tasks

import androidx.lifecycle.*
import com.mystreak.app.data.model.Task
import com.mystreak.app.data.model.TaskActivity
import com.mystreak.app.data.repository.MyStreakRepository
import com.mystreak.app.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskSortMode { ALPHABETICAL, RECENT, ACTIVITY_COUNT }

data class TaskListItem(
    val task: Task,
    val todayCount: Int,
    val lastActivityTimestamp: Long?,
    val totalCount: Int
)

class TasksViewModel(private val repo: MyStreakRepository) : ViewModel() {

    private val _sortMode = MutableLiveData(TaskSortMode.ALPHABETICAL)
    val sortMode: LiveData<TaskSortMode> = _sortMode

    val taskListItems: LiveData<List<TaskListItem>> = repo.getAllTasks()
        .combine(sortMode.asFlow()) { tasks, sort -> Pair(tasks, sort) }
        .map { (tasks, sort) -> buildItems(tasks, sort) }
        .asLiveData()

    private suspend fun buildItems(tasks: List<Task>, sort: TaskSortMode): List<TaskListItem> {
        val (todayStart, todayEnd) = DateUtils.dayBounds(DateUtils.todayEpochDay())
        return tasks.map { task ->
            val allActivities = repo.getActivitiesForTask(task.id).first()
            val todayCount = allActivities.count { it.timestamp in todayStart until todayEnd }
            val lastTimestamp = allActivities.maxOfOrNull { it.timestamp }
            TaskListItem(task, todayCount, lastTimestamp, allActivities.size)
        }.sortedWith(
            when (sort) {
                TaskSortMode.ALPHABETICAL -> compareBy { it.task.name.lowercase() }
                TaskSortMode.RECENT -> compareByDescending { it.lastActivityTimestamp ?: 0L }
                TaskSortMode.ACTIVITY_COUNT -> compareByDescending { it.totalCount }
            }
        )
    }

    fun setSortMode(mode: TaskSortMode) { _sortMode.value = mode }

    companion object {
        fun factory(repo: MyStreakRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TasksViewModel(repo) as T
            }
    }
}

package com.mystreak.app.ui.tasks

import androidx.lifecycle.*
import com.mystreak.app.data.model.Task
import com.mystreak.app.data.model.TaskActivityStats
import com.mystreak.app.data.repository.MyStreakRepository
import kotlinx.coroutines.flow.*

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

    private val _isAscending = MutableLiveData(true)
    val isAscending: LiveData<Boolean> = _isAscending

    val taskListItems: LiveData<List<TaskListItem>> =
        combine(repo.getAllTasks(), repo.observeTaskActivityStats(), _sortMode.asFlow(), _isAscending.asFlow()) {
            tasks, stats, sort, asc -> buildItems(tasks, stats, sort, asc)
        }.asLiveData()

    private fun buildItems(
        tasks: List<Task>,
        stats: List<TaskActivityStats>,
        sort: TaskSortMode,
        ascending: Boolean
    ): List<TaskListItem> {
        val statsMap = stats.associateBy { it.taskId }
        val items = tasks.map { task ->
            val s = statsMap[task.id]
            TaskListItem(task, s?.todayCount ?: 0, s?.lastTimestamp, s?.totalCount ?: 0)
        }.sortedWith(
            when (sort) {
                TaskSortMode.ALPHABETICAL -> compareBy { it.task.name.lowercase() }
                TaskSortMode.RECENT -> compareByDescending { it.lastActivityTimestamp ?: 0L }
                TaskSortMode.ACTIVITY_COUNT -> compareByDescending { it.totalCount }
            }
        )
        return if (ascending) items else items.reversed()
    }

    fun setSortMode(mode: TaskSortMode) { _sortMode.value = mode }

    fun toggleSortDirection() { _isAscending.value = !(_isAscending.value ?: true) }

    companion object {
        fun factory(repo: MyStreakRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TasksViewModel(repo) as T
            }
    }
}

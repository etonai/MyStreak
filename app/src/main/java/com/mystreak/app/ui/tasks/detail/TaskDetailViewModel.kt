package com.mystreak.app.ui.tasks.detail

import androidx.lifecycle.*
import com.mystreak.app.data.model.Task
import com.mystreak.app.data.model.TaskActivity
import com.mystreak.app.data.repository.MyStreakRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskDetailViewModel(private val repo: MyStreakRepository, private val taskId: Long) : ViewModel() {

    val task: LiveData<Task?> = repo.getTaskById(taskId).asLiveData()
    val activityHistory: LiveData<List<TaskActivity>> = repo.getActivitiesForTask(taskId).asLiveData()

    fun toggleActive() {
        viewModelScope.launch {
            val t = repo.getTaskByIdOnce(taskId) ?: return@launch
            repo.updateTask(t.copy(isActive = !t.isActive))
        }
    }

    fun deleteTask(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val t = repo.getTaskByIdOnce(taskId) ?: return@launch
            repo.deleteTask(t)
            onDeleted()
        }
    }

    companion object {
        fun factory(repo: MyStreakRepository, taskId: Long) = viewModelFactory {
            initializer { TaskDetailViewModel(repo, taskId) }
        }
    }
}

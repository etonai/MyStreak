package com.mystreak.app.ui.logging

import androidx.lifecycle.*
import com.mystreak.app.data.model.SuccessLevel
import com.mystreak.app.data.model.Task
import com.mystreak.app.data.model.TaskActivity
import com.mystreak.app.data.repository.MyStreakRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class LogStep { TASK_SELECT, LEVEL_SELECT, SUMMARY }

class LogActivityViewModel(private val repo: MyStreakRepository, preselectedTaskId: Long) : ViewModel() {

    private val _step = MutableLiveData(if (preselectedTaskId != -1L) LogStep.LEVEL_SELECT else LogStep.TASK_SELECT)
    val step: LiveData<LogStep> = _step

    private val _activeTasks = MutableLiveData<List<Task>>(emptyList())
    val activeTasks: LiveData<List<Task>> = _activeTasks

    private val _selectedTask = MutableLiveData<Task?>()
    val selectedTask: LiveData<Task?> = _selectedTask

    private val _selectedLevel = MutableLiveData<SuccessLevel?>()
    val selectedLevel: LiveData<SuccessLevel?> = _selectedLevel

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    init {
        viewModelScope.launch {
            _activeTasks.value = repo.getActiveTasks().first()
            if (preselectedTaskId != -1L) {
                _selectedTask.value = repo.getTaskByIdOnce(preselectedTaskId)
            }
        }
    }

    fun selectTask(task: Task) {
        _selectedTask.value = task
        _step.value = LogStep.LEVEL_SELECT
    }

    fun selectLevel(level: SuccessLevel) {
        _selectedLevel.value = level
        _step.value = LogStep.SUMMARY
    }

    fun goBack() {
        _step.value = when (_step.value) {
            LogStep.LEVEL_SELECT -> LogStep.TASK_SELECT
            LogStep.SUMMARY -> LogStep.LEVEL_SELECT
            else -> LogStep.TASK_SELECT
        }
    }

    fun save() {
        val task = _selectedTask.value ?: return
        val level = _selectedLevel.value ?: return
        viewModelScope.launch {
            repo.insertActivity(TaskActivity(
                taskId = task.id,
                timestamp = System.currentTimeMillis(),
                successLevel = level
            ))
            _saved.value = true
        }
    }

    companion object {
        fun factory(repo: MyStreakRepository, preselectedTaskId: Long) = viewModelFactory {
            initializer { LogActivityViewModel(repo, preselectedTaskId) }
        }
    }
}

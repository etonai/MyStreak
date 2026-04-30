package com.mystreak.app.ui.tasks.addedit

import androidx.lifecycle.*
import com.mystreak.app.data.model.Task
import com.mystreak.app.data.model.TaskPriority
import com.mystreak.app.data.repository.MyStreakRepository
import com.mystreak.app.util.ColorUtils
import kotlinx.coroutines.launch

class AddEditTaskViewModel(private val repo: MyStreakRepository, private val taskId: Long) : ViewModel() {

    val isEditMode = taskId != -1L
    val existingTask: LiveData<Task?> = if (isEditMode) repo.getTaskById(taskId).asLiveData() else MutableLiveData(null)

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun save(
        name: String,
        colorKey: String,
        priority: TaskPriority,
        minDesc: String,
        medDesc: String,
        highDesc: String
    ) {
        viewModelScope.launch {
            if (isEditMode) {
                val existing = repo.getTaskByIdOnce(taskId) ?: return@launch
                repo.updateTask(existing.copy(name = name, colorKey = colorKey, priority = priority,
                    minSuccessDesc = minDesc, medSuccessDesc = medDesc, highSuccessDesc = highDesc))
            } else {
                repo.insertTask(Task(name = name, colorKey = colorKey, priority = priority,
                    minSuccessDesc = minDesc, medSuccessDesc = medDesc, highSuccessDesc = highDesc))
            }
            _saved.value = true
        }
    }

    companion object {
        fun factory(repo: MyStreakRepository, taskId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AddEditTaskViewModel(repo, taskId) as T
            }
    }
}

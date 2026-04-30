package com.mystreak.app.ui.activityedit

import androidx.lifecycle.*
import com.mystreak.app.data.model.ActivityWithTask
import com.mystreak.app.data.model.SuccessLevel
import com.mystreak.app.data.repository.MyStreakRepository
import com.mystreak.app.util.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

class ActivityEditViewModel(private val repo: MyStreakRepository, private val activityId: Long) : ViewModel() {

    private val _activityWithTask = MutableLiveData<ActivityWithTask?>()
    val activityWithTask: LiveData<ActivityWithTask?> = _activityWithTask

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> = _deleted

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        viewModelScope.launch {
            _activityWithTask.value = repo.getActivityWithTaskByIdOnce(activityId)
        }
    }

    fun save(newTimestamp: Long, newLevel: SuccessLevel) {
        val today = DateUtils.localDateToTimestamp(LocalDate.now(), 23, 59)
        if (newTimestamp > today) {
            _error.value = "Date cannot be in the future."
            return
        }
        viewModelScope.launch {
            val awt = _activityWithTask.value ?: return@launch
            repo.updateActivity(awt.activity.copy(timestamp = newTimestamp, successLevel = newLevel))
            _saved.value = true
        }
    }

    fun delete() {
        viewModelScope.launch {
            val awt = _activityWithTask.value ?: return@launch
            repo.deleteActivity(awt.activity)
            _deleted.value = true
        }
    }

    companion object {
        fun factory(repo: MyStreakRepository, activityId: Long) = viewModelFactory {
            initializer { ActivityEditViewModel(repo, activityId) }
        }
    }
}

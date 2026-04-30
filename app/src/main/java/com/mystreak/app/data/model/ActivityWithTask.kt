package com.mystreak.app.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ActivityWithTask(
    @Embedded val activity: TaskActivity,
    @Relation(parentColumn = "taskId", entityColumn = "id")
    val task: Task
)

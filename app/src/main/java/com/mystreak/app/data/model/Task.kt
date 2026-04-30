package com.mystreak.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorKey: String,
    val priority: TaskPriority,
    val minSuccessDesc: String,
    val medSuccessDesc: String,
    val highSuccessDesc: String,
    val isActive: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis()
)

enum class TaskPriority { HIGH, LOW }

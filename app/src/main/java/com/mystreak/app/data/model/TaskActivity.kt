package com.mystreak.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TaskActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val taskId: Long,
    val timestamp: Long,
    val successLevel: SuccessLevel
)

enum class SuccessLevel { MINIMUM, MEDIUM, HIGH }

package com.mystreak.app.data.model

data class TaskActivityStats(
    val taskId: Long,
    val totalCount: Int,
    val lastTimestamp: Long?,
    val todayCount: Int
)

package com.mystreak.app.data.dao

import androidx.room.*
import com.mystreak.app.data.model.ActivityWithTask
import com.mystreak.app.data.model.TaskActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: TaskActivity): Long

    @Update
    suspend fun update(activity: TaskActivity)

    @Delete
    suspend fun delete(activity: TaskActivity)

    @Transaction
    @Query("SELECT * FROM activities WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp ASC")
    fun getActivitiesForDayWithTask(start: Long, end: Long): Flow<List<ActivityWithTask>>

    @Transaction
    @Query("SELECT * FROM activities WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp ASC")
    suspend fun getActivitiesForDayOnce(start: Long, end: Long): List<ActivityWithTask>

    @Transaction
    @Query("SELECT * FROM activities WHERE timestamp >= :start ORDER BY timestamp ASC")
    suspend fun getActivitiesFromOnce(start: Long): List<ActivityWithTask>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getByIdOnce(id: Long): TaskActivity?

    @Transaction
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getWithTaskByIdOnce(id: Long): ActivityWithTask?

    @Query("SELECT * FROM activities WHERE taskId = :taskId AND timestamp >= :start AND timestamp < :end LIMIT 1")
    suspend fun getFirstActivityForTaskOnDay(taskId: Long, start: Long, end: Long): TaskActivity?

    @Query("SELECT * FROM activities WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getActivitiesForTask(taskId: Long): Flow<List<TaskActivity>>

    @Query("SELECT COUNT(*) FROM activities WHERE taskId = :taskId AND timestamp >= :start AND timestamp < :end")
    suspend fun countForTaskOnDay(taskId: Long, start: Long, end: Long): Int

    @Query("SELECT DISTINCT taskId FROM activities WHERE timestamp >= :start AND timestamp < :end AND taskId IN (SELECT id FROM tasks WHERE priority = 'HIGH')")
    suspend fun getDistinctHighPriorityTaskIdsForDay(start: Long, end: Long): List<Long>

    @Query("SELECT COUNT(*) FROM activities WHERE taskId IN (SELECT id FROM tasks WHERE isActive = 1) AND timestamp >= :start AND timestamp < :end")
    suspend fun countActiveTaskActivitiesInRange(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM activities WHERE taskId IN (SELECT id FROM tasks WHERE isActive = 1 AND priority = 'HIGH') AND timestamp >= :start AND timestamp < :end")
    suspend fun countActiveHighPriorityActivitiesInRange(start: Long, end: Long): Int

    @Query("SELECT * FROM activities ORDER BY timestamp ASC")
    suspend fun getAllOnce(): List<TaskActivity>

    @Query("DELETE FROM activities")
    suspend fun deleteAll()
}

package com.mystreak.app.data.dao

import androidx.room.*
import com.mystreak.app.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks ORDER BY name ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskByIdOnce(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isActive = 1 AND priority = 'HIGH' ORDER BY name ASC")
    suspend fun getActiveHighPriorityTasksOnce(): List<Task>
}

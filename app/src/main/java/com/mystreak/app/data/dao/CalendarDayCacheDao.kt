package com.mystreak.app.data.dao

import androidx.room.*
import com.mystreak.app.data.model.CalendarDayCache
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDayCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: CalendarDayCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(caches: List<CalendarDayCache>)

    @Query("SELECT * FROM calendar_day_cache WHERE dateEpochDay = :epochDay")
    suspend fun getForDay(epochDay: Long): CalendarDayCache?

    @Query("SELECT * FROM calendar_day_cache")
    fun getAllFlow(): Flow<List<CalendarDayCache>>

    @Query("SELECT * FROM calendar_day_cache")
    suspend fun getAllOnce(): List<CalendarDayCache>

    @Query("DELETE FROM calendar_day_cache")
    suspend fun deleteAll()
}

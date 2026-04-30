package com.mystreak.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mystreak.app.data.dao.CalendarDayCacheDao
import com.mystreak.app.data.dao.TaskActivityDao
import com.mystreak.app.data.dao.TaskDao
import com.mystreak.app.data.model.CalendarDayCache
import com.mystreak.app.data.model.Converters
import com.mystreak.app.data.model.Task
import com.mystreak.app.data.model.TaskActivity

@Database(
    entities = [Task::class, TaskActivity::class, CalendarDayCache::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyStreakDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskActivityDao(): TaskActivityDao
    abstract fun calendarDayCacheDao(): CalendarDayCacheDao

    companion object {
        @Volatile private var instance: MyStreakDatabase? = null

        fun getDatabase(context: Context): MyStreakDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyStreakDatabase::class.java,
                    "mystreak_database"
                ).build().also { instance = it }
            }
    }
}

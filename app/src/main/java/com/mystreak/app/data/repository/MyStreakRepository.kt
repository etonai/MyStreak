package com.mystreak.app.data.repository

import com.mystreak.app.data.db.MyStreakDatabase
import com.mystreak.app.data.model.*
import com.mystreak.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MyStreakRepository(private val db: MyStreakDatabase) {

    // --- Tasks ---

    fun getAllTasks(): Flow<List<Task>> = db.taskDao().getAllTasks()
    fun getActiveTasks(): Flow<List<Task>> = db.taskDao().getActiveTasks()
    fun getTaskById(id: Long): Flow<Task?> = db.taskDao().getTaskById(id)
    suspend fun getTaskByIdOnce(id: Long): Task? = db.taskDao().getTaskByIdOnce(id)
    suspend fun getAllTasksOnce(): List<Task> = db.taskDao().getAllTasks().first()

    suspend fun insertTask(task: Task): Long = db.taskDao().insert(task)
    suspend fun updateTask(task: Task) = db.taskDao().update(task)
    suspend fun deleteTask(task: Task) = db.taskDao().delete(task)

    // --- Activities ---

    fun getActivitiesForDay(epochDay: Long): Flow<List<ActivityWithTask>> {
        val (start, end) = DateUtils.dayBounds(epochDay)
        return db.taskActivityDao().getActivitiesForDayWithTask(start, end)
    }

    suspend fun getActivitiesForDayOnce(epochDay: Long): List<ActivityWithTask> {
        val (start, end) = DateUtils.dayBounds(epochDay)
        return db.taskActivityDao().getActivitiesForDayOnce(start, end)
    }

    suspend fun getAllActivitiesOnce(): List<TaskActivity> = db.taskActivityDao().getAllOnce()

    suspend fun getActivityWithTaskByIdOnce(id: Long): ActivityWithTask? =
        db.taskActivityDao().getWithTaskByIdOnce(id)

    fun getActivitiesForTask(taskId: Long): Flow<List<TaskActivity>> =
        db.taskActivityDao().getActivitiesForTask(taskId)

    suspend fun insertActivity(activity: TaskActivity): Long =
        db.taskActivityDao().insert(activity)

    suspend fun updateActivity(activity: TaskActivity) =
        db.taskActivityDao().update(activity)

    suspend fun deleteActivity(activity: TaskActivity) =
        db.taskActivityDao().delete(activity)

    suspend fun hasTaskActivityToday(taskId: Long): Boolean {
        val (start, end) = DateUtils.dayBounds(DateUtils.todayEpochDay())
        return db.taskActivityDao().getFirstActivityForTaskOnDay(taskId, start, end) != null
    }

    suspend fun countTodayActivitiesForTask(taskId: Long): Int {
        val (start, end) = DateUtils.dayBounds(DateUtils.todayEpochDay())
        return db.taskActivityDao().countForTaskOnDay(taskId, start, end)
    }

    suspend fun getWeekSummaryCounts(): Pair<Int, Int> {
        val start = DateUtils.rollingWindowStart(7)
        val end = DateUtils.tomorrowStart()
        val total = db.taskActivityDao().countActiveTaskActivitiesInRange(start, end)
        val highPriority = db.taskActivityDao().countActiveHighPriorityActivitiesInRange(start, end)
        return Pair(total, highPriority)
    }

    suspend fun getActiveHighPriorityOutstandingTasks(): List<Task> {
        val allHighActive = db.taskDao().getActiveHighPriorityTasksOnce()
        val (start, end) = DateUtils.dayBounds(DateUtils.todayEpochDay())
        return allHighActive.filter { task ->
            db.taskActivityDao().getFirstActivityForTaskOnDay(task.id, start, end) == null
        }
    }

    // --- Calendar cache ---

    fun getAllCachedDays(): Flow<List<CalendarDayCache>> = db.calendarDayCacheDao().getAllFlow()

    suspend fun freezePastDays() {
        val today = DateUtils.todayEpochDay()
        val cached = db.calendarDayCacheDao().getAllOnce().map { it.dateEpochDay }.toSet()
        val allActivities = db.taskActivityDao().getActivitiesFromOnce(0)
        if (allActivities.isEmpty()) return

        val earliestDay = allActivities.minOf { DateUtils.timestampToEpochDay(it.activity.timestamp) }
        val daysToFreeze = (earliestDay until today).filter { it !in cached }
        if (daysToFreeze.isEmpty()) return

        val caches = daysToFreeze.map { epochDay ->
            CalendarDayCache(epochDay, computeColorForDay(epochDay))
        }
        db.calendarDayCacheDao().insertAll(caches)
    }

    suspend fun computeLiveTodayColor(): CalendarColorLevel = computeColorForDay(DateUtils.todayEpochDay())

    suspend fun computeColorForDay(epochDay: Long): CalendarColorLevel {
        val (start, end) = DateUtils.dayBounds(epochDay)
        val activities = db.taskActivityDao().getActivitiesForDayOnce(start, end)
        if (activities.isEmpty()) return CalendarColorLevel.NONE

        val highPriorityPerformed = activities
            .filter { it.task.priority == TaskPriority.HIGH }
            .map { it.activity.taskId }.toSet()

        if (highPriorityPerformed.isEmpty()) return CalendarColorLevel.LIGHT_BLUE

        val totalHighPriority = db.taskDao().getActiveHighPriorityTasksOnce().size
        if (totalHighPriority == 0) return CalendarColorLevel.LIGHT_BLUE

        return when {
            highPriorityPerformed.size >= totalHighPriority -> CalendarColorLevel.BRIGHT_GREEN
            highPriorityPerformed.size >= (totalHighPriority / 2).coerceAtLeast(1) -> CalendarColorLevel.DARK_BLUE
            else -> CalendarColorLevel.MEDIUM_BLUE
        }
    }

    // --- Import / Export ---

    suspend fun exportData(): ExportData {
        val tasks = getAllTasksOnce()
        val activities = db.taskActivityDao().getActivitiesFromOnce(0).map { it.activity }
        return ExportData(tasks, activities)
    }

    suspend fun importData(data: ExportData) {
        db.taskActivityDao().deleteAll()
        db.calendarDayCacheDao().deleteAll()
        getAllTasksOnce().forEach { db.taskDao().delete(it) }

        val oldIdToNewId = mutableMapOf<Long, Long>()
        data.tasks.forEach { task ->
            val newId = db.taskDao().insert(task.copy(id = 0))
            oldIdToNewId[task.id] = newId
        }
        data.activities.forEach { activity ->
            val newTaskId = oldIdToNewId[activity.taskId] ?: return@forEach
            db.taskActivityDao().insert(activity.copy(id = 0, taskId = newTaskId))
        }
    }
}

data class ExportData(val tasks: List<Task>, val activities: List<TaskActivity>)

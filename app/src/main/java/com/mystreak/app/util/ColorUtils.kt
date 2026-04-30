package com.mystreak.app.util

import com.mystreak.app.R

object ColorUtils {
    data class TaskColor(val key: String, val displayName: String, val colorRes: Int)

    val palette: List<TaskColor> = listOf(
        TaskColor("red", "Red", R.color.task_red),
        TaskColor("pink", "Pink", R.color.task_pink),
        TaskColor("purple", "Purple", R.color.task_purple),
        TaskColor("deep_purple", "Deep Purple", R.color.task_deep_purple),
        TaskColor("indigo", "Indigo", R.color.task_indigo),
        TaskColor("blue", "Blue", R.color.task_blue),
        TaskColor("cyan", "Cyan", R.color.task_cyan),
        TaskColor("teal", "Teal", R.color.task_teal),
        TaskColor("green", "Green", R.color.task_green),
        TaskColor("amber", "Amber", R.color.task_amber),
        TaskColor("orange", "Orange", R.color.task_orange),
        TaskColor("brown", "Brown", R.color.task_brown),
    )

    fun colorResForKey(key: String): Int =
        palette.find { it.key == key }?.colorRes ?: R.color.task_blue

    fun defaultKey(): String = "blue"
}

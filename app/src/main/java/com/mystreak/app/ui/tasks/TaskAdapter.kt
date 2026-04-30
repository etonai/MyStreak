package com.mystreak.app.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mystreak.app.databinding.ItemTaskBinding
import com.mystreak.app.util.ColorUtils

class TaskAdapter(
    private val onTaskClick: (TaskListItem) -> Unit,
    private val onLogClick: (TaskListItem) -> Unit
) : ListAdapter<TaskListItem, TaskAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val task = item.task
        with(holder.binding) {
            val colorRes = ColorUtils.colorResForKey(task.colorKey)
            viewColorDot.background.setTint(ContextCompat.getColor(root.context, colorRes))
            tvTaskName.text = task.name
            tvPriority.text = task.priority.name
            tvTodayCount.text = if (item.todayCount > 0) "${item.todayCount} today" else ""
            tvInactiveLabel.visibility = if (!task.isActive) android.view.View.VISIBLE else android.view.View.GONE
            root.alpha = if (task.isActive) 1f else 0.45f
            root.setOnClickListener { onTaskClick(item) }
            btnLog.setOnClickListener { onLogClick(item) }
            btnLog.isEnabled = task.isActive
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TaskListItem>() {
            override fun areItemsTheSame(a: TaskListItem, b: TaskListItem) = a.task.id == b.task.id
            override fun areContentsTheSame(a: TaskListItem, b: TaskListItem) = a == b
        }
    }
}

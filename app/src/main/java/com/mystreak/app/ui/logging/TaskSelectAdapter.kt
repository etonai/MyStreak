package com.mystreak.app.ui.logging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mystreak.app.data.model.Task
import com.mystreak.app.databinding.ItemTaskSelectBinding
import com.mystreak.app.util.ColorUtils

class TaskSelectAdapter(
    private val onSelect: (Task) -> Unit
) : ListAdapter<Task, TaskSelectAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemTaskSelectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemTaskSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        with(holder.binding) {
            val colorRes = ColorUtils.colorResForKey(task.colorKey)
            viewSelectColor.background.setTint(ContextCompat.getColor(root.context, colorRes))
            tvSelectTaskName.text = task.name
            tvSelectPriority.text = task.priority.name
            root.setOnClickListener { onSelect(task) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
            override fun areContentsTheSame(a: Task, b: Task) = a == b
        }
    }
}

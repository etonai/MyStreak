package com.mystreak.app.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mystreak.app.data.model.Task
import com.mystreak.app.databinding.ItemHighPriorityOutstandingBinding
import com.mystreak.app.util.ColorUtils

class HighPriorityAdapter(
    private val onLog: (Task) -> Unit
) : ListAdapter<Task, HighPriorityAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemHighPriorityOutstandingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemHighPriorityOutstandingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        with(holder.binding) {
            val colorRes = ColorUtils.colorResForKey(task.colorKey)
            viewOutstandingColor.background.setTint(ContextCompat.getColor(root.context, colorRes))
            tvOutstandingName.text = task.name
            btnOutstandingLog.setOnClickListener { onLog(task) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
            override fun areContentsTheSame(a: Task, b: Task) = a == b
        }
    }
}

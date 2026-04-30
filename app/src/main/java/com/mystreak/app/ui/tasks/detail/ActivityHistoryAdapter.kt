package com.mystreak.app.ui.tasks.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mystreak.app.data.model.TaskActivity
import com.mystreak.app.databinding.ItemActivityBinding
import com.mystreak.app.util.DateUtils

class ActivityHistoryAdapter(
    private val onEdit: (TaskActivity) -> Unit,
    private val onDelete: (TaskActivity) -> Unit
) : ListAdapter<TaskActivity, ActivityHistoryAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val activity = getItem(position)
        with(holder.binding) {
            tvActivityTime.text = DateUtils.formatDateTime(activity.timestamp)
            tvActivityTaskName.visibility = View.GONE
            tvActivityLevel.text = activity.successLevel.name.lowercase().replaceFirstChar { it.uppercase() }
            btnEdit.setOnClickListener { onEdit(activity) }
            btnDelete.setOnClickListener { onDelete(activity) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TaskActivity>() {
            override fun areItemsTheSame(a: TaskActivity, b: TaskActivity) = a.id == b.id
            override fun areContentsTheSame(a: TaskActivity, b: TaskActivity) = a == b
        }
    }
}

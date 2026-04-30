package com.mystreak.app.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mystreak.app.data.model.ActivityWithTask
import com.mystreak.app.databinding.ItemActivityBinding
import com.mystreak.app.util.ColorUtils
import com.mystreak.app.util.DateUtils

class ActivityListAdapter(
    private val onEdit: (ActivityWithTask) -> Unit,
    private val onDelete: (ActivityWithTask) -> Unit
) : ListAdapter<ActivityWithTask, ActivityListAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            val colorRes = ColorUtils.colorResForKey(item.task.colorKey)
            tvActivityTaskName.text = item.task.name
            tvActivityTaskName.setTextColor(ContextCompat.getColor(root.context, colorRes))
            tvActivityLevel.text = item.activity.successLevel.name.lowercase().replaceFirstChar { it.uppercase() }
            tvActivityTime.text = DateUtils.formatTime(item.activity.timestamp)
            btnEdit.setOnClickListener { onEdit(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ActivityWithTask>() {
            override fun areItemsTheSame(a: ActivityWithTask, b: ActivityWithTask) =
                a.activity.id == b.activity.id
            override fun areContentsTheSame(a: ActivityWithTask, b: ActivityWithTask) = a == b
        }
    }
}

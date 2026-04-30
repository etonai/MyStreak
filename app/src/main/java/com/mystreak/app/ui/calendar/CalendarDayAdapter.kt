package com.mystreak.app.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mystreak.app.R
import com.mystreak.app.data.model.CalendarColorLevel
import com.mystreak.app.databinding.ItemCalendarDayBinding

class CalendarDayAdapter(
    private val onDayClick: (Long) -> Unit
) : ListAdapter<CalendarCell, CalendarDayAdapter.VH>(DIFF) {

    private var todayColor: CalendarColorLevel = CalendarColorLevel.NONE

    inner class VH(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cell = getItem(position)
        with(holder.binding) {
            if (cell.epochDay == -1L) {
                tvDayNumber.text = ""
                viewDayBg.setBackgroundColor(0)
                root.isClickable = false
                viewTodayRing.visibility = android.view.View.GONE
                return
            }

            tvDayNumber.text = cell.dayOfMonth.toString()
            val effectiveColor = if (cell.isToday) todayColor else cell.colorLevel
            viewDayBg.setBackgroundColor(colorForLevel(root, effectiveColor))
            viewTodayRing.visibility = if (cell.isToday) android.view.View.VISIBLE else android.view.View.GONE
            root.isClickable = true
            root.setOnClickListener { onDayClick(cell.epochDay) }
        }
    }

    fun setTodayColor(level: CalendarColorLevel) {
        todayColor = level
        notifyDataSetChanged()
    }

    private fun colorForLevel(view: android.view.View, level: CalendarColorLevel): Int {
        val ctx = view.context
        return when (level) {
            CalendarColorLevel.NONE -> 0
            CalendarColorLevel.LIGHT_BLUE -> ContextCompat.getColor(ctx, R.color.calendar_light_blue)
            CalendarColorLevel.MEDIUM_BLUE -> ContextCompat.getColor(ctx, R.color.calendar_medium_blue)
            CalendarColorLevel.DARK_BLUE -> ContextCompat.getColor(ctx, R.color.calendar_dark_blue)
            CalendarColorLevel.BRIGHT_GREEN -> ContextCompat.getColor(ctx, R.color.calendar_bright_green)
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CalendarCell>() {
            override fun areItemsTheSame(a: CalendarCell, b: CalendarCell) = a.epochDay == b.epochDay
            override fun areContentsTheSame(a: CalendarCell, b: CalendarCell) = a == b
        }
    }
}

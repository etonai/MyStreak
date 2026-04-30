package com.mystreak.app.ui.calendar

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.R
import com.mystreak.app.data.model.CalendarColorLevel
import com.mystreak.app.databinding.FragmentDayActivitiesBinding
import com.mystreak.app.ui.activityedit.ActivityEditBottomSheet
import com.mystreak.app.ui.dashboard.ActivityListAdapter
import com.mystreak.app.util.DateUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class DayActivitiesBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentDayActivitiesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDayActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val epochDay = requireArguments().getLong(ARG_EPOCH_DAY)
        val colorLevelOrdinal = requireArguments().getInt(ARG_COLOR_LEVEL, 0)

        val date = DateUtils.epochDayToLocalDate(epochDay)
        binding.tvDayTitle.text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))

        val colorInt = colorForLevel(colorLevelOrdinal)
        if (colorInt != 0) {
            binding.viewColorIndicator.isVisible = true
            binding.viewColorIndicator.background.setTint(colorInt)
        }

        val repo = (requireActivity().application as MyStreakApplication).repository

        lateinit var adapter: ActivityListAdapter
        adapter = ActivityListAdapter(
            onEdit = { awt ->
                dismiss()
                ActivityEditBottomSheet.newInstance(awt.activity.id).show(parentFragmentManager, "edit_activity")
            },
            onDelete = { awt ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_activity_title)
                    .setMessage(R.string.delete_activity_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            repo.deleteActivity(awt.activity)
                            val updated = repo.getActivitiesForDayOnce(epochDay)
                            withContext(Dispatchers.Main) {
                                if (_binding == null) return@withContext
                                adapter.submitList(updated)
                                binding.tvNoDayActivities.isVisible = updated.isEmpty()
                                binding.rvDayActivities.isVisible = updated.isNotEmpty()
                            }
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )

        binding.rvDayActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDayActivities.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val activities = repo.getActivitiesForDayOnce(epochDay)
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                adapter.submitList(activities)
                binding.tvNoDayActivities.isVisible = activities.isEmpty()
                binding.rvDayActivities.isVisible = activities.isNotEmpty()
            }
        }
    }

    private fun colorForLevel(ordinal: Int): Int {
        val ctx = requireContext()
        return when (CalendarColorLevel.values()[ordinal]) {
            CalendarColorLevel.NONE -> 0
            CalendarColorLevel.LIGHT_BLUE -> ContextCompat.getColor(ctx, R.color.calendar_light_blue)
            CalendarColorLevel.MEDIUM_BLUE -> ContextCompat.getColor(ctx, R.color.calendar_medium_blue)
            CalendarColorLevel.DARK_BLUE -> ContextCompat.getColor(ctx, R.color.calendar_dark_blue)
            CalendarColorLevel.BRIGHT_GREEN -> ContextCompat.getColor(ctx, R.color.calendar_bright_green)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EPOCH_DAY = "epoch_day"
        private const val ARG_COLOR_LEVEL = "color_level"

        fun newInstance(epochDay: Long, colorLevel: Int = 0) = DayActivitiesBottomSheet().apply {
            arguments = bundleOf(ARG_EPOCH_DAY to epochDay, ARG_COLOR_LEVEL to colorLevel)
        }
    }
}

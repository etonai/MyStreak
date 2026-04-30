package com.mystreak.app.ui.calendar

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.databinding.FragmentDayActivitiesBinding
import com.mystreak.app.ui.activityedit.ActivityEditBottomSheet
import com.mystreak.app.ui.dashboard.ActivityListAdapter
import com.mystreak.app.util.DateUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
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
        val date = DateUtils.epochDayToLocalDate(epochDay)
        binding.tvDayTitle.text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))

        val repo = (requireActivity().application as MyStreakApplication).repository
        val adapter = ActivityListAdapter { awt ->
            dismiss()
            ActivityEditBottomSheet.newInstance(awt.activity.id).show(parentFragmentManager, "edit_activity")
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EPOCH_DAY = "epoch_day"
        fun newInstance(epochDay: Long) = DayActivitiesBottomSheet().apply {
            arguments = bundleOf(ARG_EPOCH_DAY to epochDay)
        }
    }
}

package com.mystreak.app.ui.calendar

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy { (requireActivity().application as MyStreakApplication).repository }
    private val viewModel by viewModels<CalendarViewModel> { CalendarViewModel.factory(repo) }
    private lateinit var calendarAdapter: CalendarDayAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        calendarAdapter = CalendarDayAdapter { epochDay ->
            DayActivitiesBottomSheet.newInstance(epochDay)
                .show(childFragmentManager, "day_activities")
        }

        // 7 columns for days of the week; each cell height matches its width
        val gridLayoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvCalendar.layoutManager = gridLayoutManager
        binding.rvCalendar.adapter = calendarAdapter

        // Make cells square via a custom ItemDecoration
        binding.rvCalendar.addItemDecoration(SquareCellDecoration())

        viewModel.monthLabel.observe(viewLifecycleOwner) { label ->
            binding.tvMonthYear.text = label
        }

        viewModel.calendarCells.observe(viewLifecycleOwner) { cells ->
            calendarAdapter.submitList(cells)
            // Refresh today's live color
            viewModel.refreshTodayColor { level ->
                calendarAdapter.setTodayColor(level)
            }
        }

        binding.btnPrevMonth.setOnClickListener { viewModel.goToPreviousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.goToNextMonth() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTodayColor { level -> calendarAdapter.setTodayColor(level) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

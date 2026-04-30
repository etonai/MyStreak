package com.mystreak.app.ui.dashboard

import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.R
import com.mystreak.app.databinding.FragmentDashboardBinding
import com.mystreak.app.ui.activityedit.ActivityEditBottomSheet
import com.mystreak.app.ui.logging.LogActivityBottomSheet
import com.mystreak.app.util.JsonExporter
import com.mystreak.app.util.JsonImporter
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy { (requireActivity().application as MyStreakApplication).repository }
    private val viewModel by viewModels<DashboardViewModel> { DashboardViewModel.factory(repo) }

    private lateinit var todayAdapter: ActivityListAdapter
    private lateinit var yesterdayAdapter: ActivityListAdapter
    private lateinit var outstandingAdapter: HighPriorityAdapter

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            val data = repo.exportData()
            val result = JsonExporter.export(requireContext(), uri, data)
            val msg = if (result.isSuccess) getString(R.string.export_success)
            else getString(R.string.export_error, result.exceptionOrNull()?.message)
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }
    }

    private val importPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.import_warning_title)
            .setMessage(R.string.import_warning_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                lifecycleScope.launch {
                    val result = JsonImporter.import(requireContext(), uri)
                    if (result.isSuccess) {
                        val data = result.getOrThrow()
                        repo.importData(data)
                        val msg = getString(R.string.import_success, data.tasks.size, data.activities.size)
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                        viewModel.refresh()
                    } else {
                        Snackbar.make(binding.root, getString(R.string.import_error, result.exceptionOrNull()?.message), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        todayAdapter = ActivityListAdapter(
            onEdit = { awt ->
                ActivityEditBottomSheet.newInstance(awt.activity.id).show(childFragmentManager, "edit_activity")
            },
            onDelete = { awt ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_activity_title)
                    .setMessage(R.string.delete_activity_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        lifecycleScope.launch { repo.deleteActivity(awt.activity) }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        yesterdayAdapter = ActivityListAdapter(
            onEdit = { awt ->
                ActivityEditBottomSheet.newInstance(awt.activity.id).show(childFragmentManager, "edit_activity")
            },
            onDelete = { awt ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_activity_title)
                    .setMessage(R.string.delete_activity_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        lifecycleScope.launch { repo.deleteActivity(awt.activity) }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        outstandingAdapter = HighPriorityAdapter { task ->
            LogActivityBottomSheet.newInstance(task.id).show(childFragmentManager, "log_activity")
        }

        binding.rvToday.layoutManager = LinearLayoutManager(requireContext())
        binding.rvToday.adapter = todayAdapter
        binding.rvYesterday.layoutManager = LinearLayoutManager(requireContext())
        binding.rvYesterday.adapter = yesterdayAdapter
        binding.rvOutstanding.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOutstanding.adapter = outstandingAdapter

        viewModel.todayActivities.observe(viewLifecycleOwner) { list ->
            todayAdapter.submitList(list)
            binding.tvNoToday.isVisible = list.isEmpty()
            binding.tvTodayHeader.text = getString(R.string.today_header, list.size)
        }
        viewModel.yesterdayActivities.observe(viewLifecycleOwner) { list ->
            yesterdayAdapter.submitList(list)
            binding.tvNoYesterday.isVisible = list.isEmpty()
            binding.tvYesterdayHeader.text = getString(R.string.yesterday_header, list.size)
        }
        viewModel.streak.observe(viewLifecycleOwner) { count ->
            binding.tvStreakCount.text = if (count > 0) "${count} days 🔥" else "0 days"
        }
        viewModel.weekSummary.observe(viewLifecycleOwner) { (total, high) ->
            binding.tvWeekSummary.text = getString(R.string.week_summary_text, total, high)
        }
        viewModel.outstandingTasks.observe(viewLifecycleOwner) { tasks ->
            outstandingAdapter.submitList(tasks)
            binding.tvOutstandingDone.isVisible = tasks.isEmpty()
            binding.rvOutstanding.isVisible = tasks.isNotEmpty()
        }

        binding.fabAddActivity.setOnClickListener {
            LogActivityBottomSheet.newInstance(-1L).show(childFragmentManager, "log_activity")
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> { exportLauncher.launch("mystreak_export.json"); true }
            R.id.action_import -> { importPickerLauncher.launch(arrayOf("application/json", "*/*")); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

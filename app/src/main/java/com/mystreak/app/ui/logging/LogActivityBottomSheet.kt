package com.mystreak.app.ui.logging

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.data.model.SuccessLevel
import com.mystreak.app.databinding.FragmentLogActivityBinding
import com.mystreak.app.util.DateUtils

class LogActivityBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentLogActivityBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy { (requireActivity().application as MyStreakApplication).repository }
    private val viewModel by viewModels<LogActivityViewModel> {
        val taskId = requireArguments().getLong(ARG_TASK_ID, -1L)
        LogActivityViewModel.factory(repo, taskId)
    }

    private lateinit var taskSelectAdapter: TaskSelectAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLogActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        taskSelectAdapter = TaskSelectAdapter { task -> viewModel.selectTask(task) }
        binding.rvTaskSelect.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTaskSelect.adapter = taskSelectAdapter

        viewModel.activeTasks.observe(viewLifecycleOwner) { tasks ->
            taskSelectAdapter.submitList(tasks)
        }

        viewModel.step.observe(viewLifecycleOwner) { step ->
            binding.layoutStepTask.isVisible = step == LogStep.TASK_SELECT
            binding.layoutStepLevel.isVisible = step == LogStep.LEVEL_SELECT
            binding.layoutStepSummary.isVisible = step == LogStep.SUMMARY
            binding.btnLogBack.isVisible = step != LogStep.TASK_SELECT

            when (step) {
                LogStep.TASK_SELECT -> binding.tvLogTitle.setText(com.mystreak.app.R.string.select_task)
                LogStep.LEVEL_SELECT -> {
                    binding.tvLogTitle.setText(com.mystreak.app.R.string.select_success_level)
                    val task = viewModel.selectedTask.value
                    if (task != null) {
                        binding.btnLevelMin.text = "Minimum — ${task.minSuccessDesc}"
                        binding.btnLevelMed.text = "Medium — ${task.medSuccessDesc}"
                        binding.btnLevelHigh.text = "High — ${task.highSuccessDesc}"
                    }
                }
                LogStep.SUMMARY -> {
                    binding.tvLogTitle.setText(com.mystreak.app.R.string.log_summary_title)
                    binding.tvSummaryTask.text = "Task: ${viewModel.selectedTask.value?.name}"
                    binding.tvSummaryLevel.text = "Level: ${viewModel.selectedLevel.value?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}"
                    binding.tvSummaryTime.text = "Time: ${DateUtils.formatTime(System.currentTimeMillis())}"
                }
            }
        }

        binding.btnLevelMin.setOnClickListener { viewModel.selectLevel(SuccessLevel.MINIMUM) }
        binding.btnLevelMed.setOnClickListener { viewModel.selectLevel(SuccessLevel.MEDIUM) }
        binding.btnLevelHigh.setOnClickListener { viewModel.selectLevel(SuccessLevel.HIGH) }

        binding.btnSaveActivity.setOnClickListener { viewModel.save() }
        binding.btnLogBack.setOnClickListener { viewModel.goBack() }
        binding.btnLogCancel.setOnClickListener { dismiss() }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TASK_ID = "task_id"
        fun newInstance(taskId: Long) = LogActivityBottomSheet().apply {
            arguments = bundleOf(ARG_TASK_ID to taskId)
        }
    }
}

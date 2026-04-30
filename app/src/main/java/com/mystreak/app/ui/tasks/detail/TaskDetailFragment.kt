package com.mystreak.app.ui.tasks.detail

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.R
import com.mystreak.app.databinding.FragmentTaskDetailBinding
import com.mystreak.app.ui.activityedit.ActivityEditBottomSheet
import com.mystreak.app.ui.logging.LogActivityBottomSheet
import com.mystreak.app.util.ColorUtils

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val args: TaskDetailFragmentArgs by navArgs()
    private val repo by lazy { (requireActivity().application as MyStreakApplication).repository }
    private val viewModel by viewModels<TaskDetailViewModel> {
        TaskDetailViewModel.factory(repo, args.taskId)
    }
    private lateinit var historyAdapter: ActivityHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        historyAdapter = ActivityHistoryAdapter(
            onEdit = { activity ->
                ActivityEditBottomSheet.newInstance(activity.id).show(childFragmentManager, "edit_activity")
            },
            onDelete = { activity ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_activity_title)
                    .setMessage(R.string.delete_activity_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewModel.deleteActivity(activity)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.rvActivityHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivityHistory.adapter = historyAdapter

        viewModel.task.observe(viewLifecycleOwner) { task ->
            if (task == null) return@observe
            binding.tvTaskName.text = task.name
            val colorRes = ColorUtils.colorResForKey(task.colorKey)
            binding.viewTaskColor.background.setTint(ContextCompat.getColor(requireContext(), colorRes))
            binding.tvPriorityBadge.text = task.priority.name
            binding.tvActiveStatus.text = if (task.isActive) "Active" else "Inactive"
            binding.tvMinDesc.text = task.minSuccessDesc
            binding.tvMedDesc.text = task.medSuccessDesc
            binding.tvHighDesc.text = task.highSuccessDesc
            binding.btnToggleActive.text = if (task.isActive) "Set Inactive" else "Set Active"
            binding.btnLogActivity.isEnabled = task.isActive
        }

        viewModel.activityHistory.observe(viewLifecycleOwner) { activities ->
            val sorted = activities.sortedByDescending { it.timestamp }
            historyAdapter.submitList(sorted)
            binding.tvNoHistory.isVisible = activities.isEmpty()
            binding.rvActivityHistory.isVisible = activities.isNotEmpty()
        }

        binding.btnLogActivity.setOnClickListener {
            LogActivityBottomSheet.newInstance(args.taskId).show(childFragmentManager, "log")
        }

        binding.btnEditTask.setOnClickListener {
            val action = TaskDetailFragmentDirections.actionTaskDetailToAddEditTask(args.taskId)
            findNavController().navigate(action)
        }

        binding.btnToggleActive.setOnClickListener {
            viewModel.toggleActive()
        }

        binding.btnDeleteTask.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_task_title)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteTask { findNavController().popBackStack() }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

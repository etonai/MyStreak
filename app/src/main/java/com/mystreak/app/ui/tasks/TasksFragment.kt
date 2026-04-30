package com.mystreak.app.ui.tasks

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.R
import com.mystreak.app.databinding.FragmentTasksBinding
import com.mystreak.app.ui.logging.LogActivityBottomSheet

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy { (requireActivity().application as MyStreakApplication).repository }
    private val viewModel by viewModels<TasksViewModel> { TasksViewModel.factory(repo) }
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TaskAdapter(
            onTaskClick = { item ->
                val action = TasksFragmentDirections.actionTasksToTaskDetail(item.task.id)
                findNavController().navigate(action)
            },
            onLogClick = { item ->
                LogActivityBottomSheet.newInstance(item.task.id).show(childFragmentManager, "log")
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        viewModel.taskListItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.tvNoTasks.isVisible = items.isEmpty()
            binding.rvTasks.isVisible = items.isNotEmpty()
        }

        binding.chipGroupSort.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_recent -> viewModel.setSortMode(TaskSortMode.RECENT)
                R.id.chip_count -> viewModel.setSortMode(TaskSortMode.ACTIVITY_COUNT)
                else -> viewModel.setSortMode(TaskSortMode.ALPHABETICAL)
            }
        }

        binding.fabAddTask.setOnClickListener {
            val action = TasksFragmentDirections.actionTasksToAddEditTask(-1L)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

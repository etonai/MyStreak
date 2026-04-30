package com.mystreak.app.ui.tasks.addedit

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.R
import com.mystreak.app.data.model.TaskPriority
import com.mystreak.app.databinding.FragmentAddEditTaskBinding
import com.mystreak.app.util.ColorUtils

class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditTaskFragmentArgs by navArgs()
    private val repo by lazy { (requireActivity().application as MyStreakApplication).repository }
    private val viewModel by viewModels<AddEditTaskViewModel> {
        AddEditTaskViewModel.factory(repo, args.taskId)
    }
    private lateinit var swatchAdapter: ColorSwatchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swatchAdapter = ColorSwatchAdapter { /* selected key updated in adapter */ }
        binding.rvColorPalette.layoutManager = GridLayoutManager(requireContext(), 6)
        binding.rvColorPalette.adapter = swatchAdapter

        // Default to High priority
        binding.togglePriority.check(R.id.btn_priority_high)

        if (viewModel.isEditMode) {
            viewModel.existingTask.observe(viewLifecycleOwner) { task ->
                task ?: return@observe
                binding.etTaskName.setText(task.name)
                swatchAdapter.setSelected(task.colorKey)
                when (task.priority) {
                    TaskPriority.HIGH -> binding.togglePriority.check(R.id.btn_priority_high)
                    TaskPriority.LOW -> binding.togglePriority.check(R.id.btn_priority_low)
                }
                binding.etMinSuccess.setText(task.minSuccessDesc)
                binding.etMedSuccess.setText(task.medSuccessDesc)
                binding.etHighSuccess.setText(task.highSuccessDesc)
            }
        }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) findNavController().popBackStack()
        }

        binding.btnSaveTask.setOnClickListener {
            val name = binding.etTaskName.text?.toString()?.trim() ?: ""
            if (name.isBlank()) {
                binding.etTaskName.error = "Name is required"
                return@setOnClickListener
            }
            val priority = when (binding.togglePriority.checkedButtonId) {
                R.id.btn_priority_low -> TaskPriority.LOW
                else -> TaskPriority.HIGH
            }
            viewModel.save(
                name = name,
                colorKey = swatchAdapter.getSelectedKey(),
                priority = priority,
                minDesc = binding.etMinSuccess.text?.toString() ?: "",
                medDesc = binding.etMedSuccess.text?.toString() ?: "",
                highDesc = binding.etHighSuccess.text?.toString() ?: ""
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

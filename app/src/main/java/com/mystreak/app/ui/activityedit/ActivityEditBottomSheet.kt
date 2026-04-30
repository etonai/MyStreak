package com.mystreak.app.ui.activityedit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mystreak.app.MyStreakApplication
import com.mystreak.app.R
import com.mystreak.app.data.model.SuccessLevel
import com.mystreak.app.databinding.FragmentActivityEditBinding
import com.mystreak.app.util.DateUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ActivityEditBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentActivityEditBinding? = null
    private val binding get() = _binding!!

    private val repo by lazy { (requireActivity().application as MyStreakApplication).repository }
    private val viewModel by viewModels<ActivityEditViewModel> {
        ActivityEditViewModel.factory(repo, requireArguments().getLong(ARG_ACTIVITY_ID))
    }

    private var selectedTimestamp = System.currentTimeMillis()
    private var selectedLevel = SuccessLevel.MINIMUM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActivityEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.activityWithTask.observe(viewLifecycleOwner) { awt ->
            awt ?: return@observe
            binding.tvEditTaskName.text = awt.task.name
            selectedTimestamp = awt.activity.timestamp
            selectedLevel = awt.activity.successLevel
            updateDateTimeButton()
            when (awt.activity.successLevel) {
                SuccessLevel.MINIMUM -> binding.toggleLevel.check(R.id.btn_edit_min)
                SuccessLevel.MEDIUM -> binding.toggleLevel.check(R.id.btn_edit_med)
                SuccessLevel.HIGH -> binding.toggleLevel.check(R.id.btn_edit_high)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        binding.toggleLevel.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedLevel = when (checkedId) {
                R.id.btn_edit_med -> SuccessLevel.MEDIUM
                R.id.btn_edit_high -> SuccessLevel.HIGH
                else -> SuccessLevel.MINIMUM
            }
        }

        binding.btnPickDatetime.setOnClickListener { showDateTimePicker() }

        binding.btnSaveEdit.setOnClickListener {
            viewModel.save(selectedTimestamp, selectedLevel)
        }

        binding.btnDeleteActivity.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_activity_title)
                .setMessage(R.string.delete_activity_message)
                .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.btnCancelEdit.setOnClickListener { dismiss() }

        viewModel.saved.observe(viewLifecycleOwner) { if (it) dismiss() }
        viewModel.deleted.observe(viewLifecycleOwner) { if (it) dismiss() }
    }

    private fun updateDateTimeButton() {
        binding.btnPickDatetime.text = DateUtils.formatDateTime(selectedTimestamp)
    }

    private fun showDateTimePicker() {
        val zone = ZoneId.systemDefault()
        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(selectedTimestamp), zone)
        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val newTimestamp = DateUtils.localDateToTimestamp(
                    java.time.LocalDate.of(year, month + 1, day), hour, minute
                )
                selectedTimestamp = newTimestamp
                updateDateTimeButton()
            }, ldt.hour, ldt.minute, false).show()
        }, ldt.year, ldt.monthValue - 1, ldt.dayOfMonth).also { dialog ->
            // Prevent future dates
            dialog.datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ACTIVITY_ID = "activity_id"
        fun newInstance(activityId: Long) = ActivityEditBottomSheet().apply {
            arguments = bundleOf(ARG_ACTIVITY_ID to activityId)
        }
    }
}

package com.dan.school.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.dan.school.DataViewModel
import com.dan.school.R
import com.dan.school.School
import com.dan.school.School.categoryChipBackgroundColorStateList
import com.dan.school.School.categoryChipStrokeColorStateList
import com.dan.school.School.categoryColors
import com.dan.school.databinding.LayoutAddBottomSheetBinding
import com.dan.school.models.Item
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AddBottomSheetDialogFragment(
    private val listener: GoToEditFragment,
    private val categoryChangeListener: SelectedCategoryChangeListener,
    private var category: Int,
    selectedCalendarDate: Calendar?
) :
    BottomSheetDialogFragment(), DatePickerDialog.OnDateSetListener,
    DatePickerFragment.OnCancelListener {

    private var _binding: LayoutAddBottomSheetBinding? = null

    private val binding get() = _binding!!

    private lateinit var inputMethodManager: InputMethodManager

    private var selectedDate = Calendar.getInstance()
    private var isInitialDateFromCalendarFragment = false
    private val dateToday = Calendar.getInstance()
    private val dateTomorrow = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat(School.dateFormat, Locale.getDefault())

    private var chipGroupSelected: Int = School.TODAY

    private val dataViewModel: DataViewModel by activityViewModels()

    init {
        if (selectedCalendarDate != null) {
            selectedDate = selectedCalendarDate
            isInitialDateFromCalendarFragment = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            0
        )
        _binding = LayoutAddBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // listeners
        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheet.parent.parent.requestLayout()
            }
        }
        binding.buttonShowAllDetails.setOnClickListener {
            when (chipGroupSelected) {
                School.PICK_DATE -> {
                    listener.goToEditFragment(
                        category,
                        binding.editTextTitle.text.toString(),
                        School.PICK_DATE,
                        selectedDate
                    )
                }
                else -> {
                    listener.goToEditFragment(
                        category,
                        binding.editTextTitle.text.toString(),
                        chipGroupSelected,
                        null
                    )
                }
            }
        }
        binding.chipHomework.setOnClickListener {
            changeColors(School.HOMEWORK)
        }
        binding.chipExam.setOnClickListener {
            changeColors(School.EXAM)
        }
        binding.chipTask.setOnClickListener {
            changeColors(School.TASK)
        }
        binding.chipGroupDate.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipToday -> {
                    binding.textViewDatePicked.text = dateFormat.format(dateToday.time)
                    selectedDate = dateToday
                    chipGroupSelected = School.TODAY
                }
                R.id.chipTomorrow -> {
                    binding.textViewDatePicked.text = dateFormat.format(dateTomorrow.time)
                    selectedDate = dateTomorrow
                    chipGroupSelected = School.TOMORROW
                }
            }
        }
        binding.chipPickDate.setOnClickListener {
            val datePicker: DialogFragment =
                DatePickerFragment(this, this)
            datePicker.show(childFragmentManager, null)
        }
        binding.buttonCheck.setOnClickListener {
            val item = Item(
                category = category,
                title = binding.editTextTitle.text.toString(),
                date = SimpleDateFormat(School.dateFormatOnDatabase, Locale.getDefault()).format(
                    selectedDate.time
                ).toInt()
            )
            dataViewModel.insert(item)
            dismiss()
        }

        dateTomorrow.add(Calendar.DAY_OF_MONTH, 1)
        changeColors(category)
        if (isInitialDateFromCalendarFragment) {
            binding.chipGroupDate.check(R.id.chipPickDate)
            dateSet(selectedDate[Calendar.YEAR], selectedDate[Calendar.MONTH], selectedDate[Calendar.DAY_OF_MONTH])
        } else {
            binding.chipGroupDate.check(R.id.chipToday)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Changes views' colors depending on [newCategory]
     *
     * [newCategory] One of [School.HOMEWORK], [School.EXAM],
     * or [School.TASK]
     */
    private fun changeColors(newCategory: Int) {
        val colorTo = ContextCompat.getColorStateList(requireContext(), categoryColors[newCategory])
        val chipBackgroundColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipBackgroundColorStateList[newCategory]
        )
        val chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[newCategory]
        )

        when {
            binding.chipHomework.isSelected -> {
                binding.chipHomework.isSelected = false
            }
            binding.chipExam.isSelected -> {
                binding.chipExam.isSelected = false
            }
            binding.chipTask.isSelected -> {
                binding.chipTask.isSelected = false
            }
        }
        when (newCategory) {
            School.HOMEWORK -> {
                binding.chipHomework.chipBackgroundColor = chipBackgroundColor
                binding.chipHomework.isSelected = true

            }
            School.EXAM -> {
                binding.chipExam.chipBackgroundColor = chipBackgroundColor
                binding.chipExam.isSelected = true
            }
            School.TASK -> {
                binding.chipTask.chipBackgroundColor = chipBackgroundColor
                binding.chipTask.isSelected = true
            }
        }
        binding.editTextTitle.background.mutate().setTintList(colorTo)
        binding.textViewDatePicked.setTextColor(colorTo)
        binding.buttonCheck.imageTintList = colorTo
        binding.buttonShowAllDetails.setTextColor(colorTo)
        (binding.buttonShowAllDetails as MaterialButton).iconTint = colorTo

        binding.chipPickDate.chipBackgroundColor = chipBackgroundColor
        binding.chipToday.chipBackgroundColor = chipBackgroundColor
        binding.chipTomorrow.chipBackgroundColor = chipBackgroundColor
        binding.chipPickDate.chipStrokeColor = chipStrokeColor
        binding.chipToday.chipStrokeColor = chipStrokeColor
        binding.chipTomorrow.chipStrokeColor = chipStrokeColor

        category = newCategory
        categoryChangeListener.selectedCategoryChanged(newCategory)
    }

    private fun dateSet(year: Int, month: Int, dayOfMonth: Int) {
        selectedDate.set(Calendar.YEAR, year)
        selectedDate.set(Calendar.MONTH, month)
        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        binding.textViewDatePicked.text = dateFormat.format(selectedDate.time)
        chipGroupSelected = School.PICK_DATE
    }

    override fun onDismiss(dialog: DialogInterface) {
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        super.onDismiss(dialog)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dateSet(year, month, dayOfMonth)
    }

    override fun canceled() {
        when (chipGroupSelected) {
            School.TODAY -> binding.chipGroupDate.check(R.id.chipToday)
            School.TOMORROW -> binding.chipGroupDate.check(R.id.chipTomorrow)
            School.PICK_DATE -> binding.chipGroupDate.check(R.id.chipPickDate)
        }
    }

    interface SelectedCategoryChangeListener {
        fun selectedCategoryChanged(category: Int)
    }

    interface GoToEditFragment {
        fun goToEditFragment(
            category: Int,
            title: String,
            chipGroupDateSelected: Int,
            date: Calendar?
        )
    }
}
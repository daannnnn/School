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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.dan.school.DataViewModel
import com.dan.school.R
import com.dan.school.School
import com.dan.school.models.Item
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.*
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.buttonCheck
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.chipGroupDate
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.chipPickDate
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.chipToday
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.chipTomorrow
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.editTextTitle
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.textViewDatePicked
import java.text.SimpleDateFormat
import java.util.*

class AddBottomSheetDialogFragment(
    private val listener: GoToEditFragment,
    private val categoryChangeListener: SelectedCategoryChangeListener,
    private var category: Int
) :
    BottomSheetDialogFragment(), DatePickerDialog.OnDateSetListener,
    DatePickerFragment.OnCancelListener {

    private lateinit var inputMethodManager: InputMethodManager
    private val categoryColors =
        arrayOf(
            R.color.homeworkColor,
            R.color.examColor,
            R.color.taskColor,
            R.color.colorPrimary
        )
    private val categoryChipBackgroundColorStateList = arrayOf(
        R.color.chip_homework_background_state_list,
        R.color.chip_exam_background_state_list,
        R.color.chip_task_background_state_list,
        R.color.chip_background_state_list
    )
    private val categoryChipStrokeColorStateList = arrayOf(
        R.color.chip_homework_stroke_color_state_list,
        R.color.chip_exam_stroke_color_state_list,
        R.color.chip_task_stroke_color_state_list,
        R.color.chip_stroke_color_state_list
    )
    private val selectedDate = Calendar.getInstance()
    private val dateToday = Calendar.getInstance()
    private val dateTomorrow = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat(School.dateFormat, Locale.getDefault())

    private var chipGroupDateSelected: Int = R.id.chipToday

    private lateinit var dataViewModel: DataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            0
        )
        return inflater.inflate(R.layout.layout_add_bottom_sheet, container, false)
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
        buttonShowAllDetails.setOnClickListener {
            when (chipGroupDateSelected) {
                R.id.chipToday -> {
                    listener.goToEditFragment(
                        category,
                        editTextTitle.text.toString(),
                        School.TODAY,
                        null
                    )
                }
                R.id.chipTomorrow -> {
                    listener.goToEditFragment(
                        category,
                        editTextTitle.text.toString(),
                        School.TOMORROW,
                        null
                    )
                }
                else -> {
                    listener.goToEditFragment(
                        category,
                        editTextTitle.text.toString(),
                        School.PICK_DATE,
                        selectedDate
                    )
                }
            }
        }
        chipHomework.setOnClickListener {
            changeColors(School.HOMEWORK)
        }
        chipExam.setOnClickListener {
            changeColors(School.EXAM)
        }
        chipTask.setOnClickListener {
            changeColors(School.TASK)
        }
        chipGroupDate.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipPickDate -> {
                    val datePicker: DialogFragment =
                        DatePickerFragment(this, this)
                    datePicker.show(childFragmentManager, "date picker")
                }
                R.id.chipToday -> {
                    textViewDatePicked.text = dateFormat.format(dateToday.time)
                    chipGroupDateSelected = R.id.chipToday
                }
                R.id.chipTomorrow -> {
                    textViewDatePicked.text = dateFormat.format(dateTomorrow.time)
                    chipGroupDateSelected = R.id.chipTomorrow
                }
            }
        }
        buttonCheck.setOnClickListener {
            val item = Item(
                category = category,
                title = editTextTitle.text.toString(),
                date = dateFormat.format(selectedDate.time)
            )
            dataViewModel.insert(item)
            dismiss()
        }

        // initialize
        dateTomorrow.add(Calendar.DAY_OF_MONTH, 1)
        changeColors(category)
        chipGroupDate.check(R.id.chipToday)
    }

    interface GoToEditFragment {
        fun goToEditFragment(
            category: Int,
            title: String,
            chipGroupDateSelected: Int,
            date: Calendar?
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        super.onDismiss(dialog)
    }

    private fun changeColors(newCategory: Int) {
        val colorTo = ContextCompat.getColorStateList(requireContext(), categoryColors[newCategory])
        when {
            chipHomework.isSelected -> {
                chipHomework.isSelected = false
            }
            chipExam.isSelected -> {
                chipExam.isSelected = false
            }
            chipTask.isSelected -> {
                chipTask.isSelected = false
            }
        }
        when (newCategory) {
            School.HOMEWORK -> {
                chipHomework.chipBackgroundColor = ContextCompat.getColorStateList(
                    requireContext(),
                    categoryChipBackgroundColorStateList[newCategory]
                )
                chipHomework.isSelected = true

            }
            School.EXAM -> {
                chipExam.chipBackgroundColor = ContextCompat.getColorStateList(
                    requireContext(),
                    categoryChipBackgroundColorStateList[newCategory]
                )
                chipExam.isSelected = true
            }
            School.TASK -> {
                chipTask.chipBackgroundColor = ContextCompat.getColorStateList(
                    requireContext(),
                    categoryChipBackgroundColorStateList[newCategory]
                )
                chipTask.isSelected = true
            }
        }
        editTextTitle.background.mutate().setTintList(colorTo)
        textViewDatePicked.setTextColor(colorTo)
        buttonCheck.imageTintList = colorTo
        buttonShowAllDetails.setTextColor(colorTo)
        (buttonShowAllDetails as MaterialButton).iconTint = colorTo
        chipPickDate.chipBackgroundColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipBackgroundColorStateList[newCategory]
        )
        chipToday.chipBackgroundColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipBackgroundColorStateList[newCategory]
        )
        chipTomorrow.chipBackgroundColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipBackgroundColorStateList[newCategory]
        )
        chipPickDate.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[newCategory]
        )
        chipToday.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[newCategory]
        )
        chipTomorrow.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[newCategory]
        )
        category = newCategory
        categoryChangeListener.selectedCategoryChanged(newCategory)
    }

    interface SelectedCategoryChangeListener {
        fun selectedCategoryChanged(category: Int)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        selectedDate.set(Calendar.YEAR, year)
        selectedDate.set(Calendar.MONTH, month)
        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        textViewDatePicked.text = dateFormat.format(selectedDate.time)
        chipGroupDateSelected = R.id.chipPickDate
    }

    override fun canceled() {
        chipGroupDate.check(chipGroupDateSelected)
    }
}
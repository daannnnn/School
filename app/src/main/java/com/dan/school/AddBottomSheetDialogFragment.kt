package com.dan.school

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.layout_add_bottom_sheet.*

class AddBottomSheetDialogFragment(private var listener: GoToEditFragment, var categoryChangeListener: SelectedCategoryChangeListener, private val category: Int) :
    BottomSheetDialogFragment() {

    private lateinit var inputMethodManager: InputMethodManager
    private val categoryColors =
        arrayOf(R.color.homeworkColor, R.color.examColor, R.color.taskColor, R.color.colorPrimary)
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
            listener.goToEditFragment()
        }
        chipHomework.setOnClickListener {
            changeColors(School.Category.HOMEWORK)
        }
        chipExam.setOnClickListener {
            changeColors(School.Category.EXAM)
        }
        chipTask.setOnClickListener {
            changeColors(School.Category.TASK)
        }

        changeColors(category)
    }

    interface GoToEditFragment {
        fun goToEditFragment()
    }

    override fun onDismiss(dialog: DialogInterface) {
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        super.onDismiss(dialog)
    }

    private fun changeColors(category: Int) {
        val colorFrom: Int
        when {
            chipHomework.isSelected -> {
                colorFrom = ContextCompat.getColor(requireContext(), R.color.homeworkColor)
                chipHomework.isSelected = false
            }
            chipExam.isSelected -> {
                colorFrom = ContextCompat.getColor(requireContext(), R.color.examColor)
                chipExam.isSelected = false
            }
            chipTask.isSelected -> {
                colorFrom = ContextCompat.getColor(requireContext(), R.color.taskColor)
                chipTask.isSelected = false
            }
            else -> {
                colorFrom = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            }
        }
        val colorTo = ContextCompat.getColor(requireContext(), categoryColors[category])
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animator ->
            editTextTitle.background.mutate().setTint(animator.animatedValue as Int)
            buttonCheck.drawable.setTint(animator.animatedValue as Int)
            buttonShowAllDetails.setTextColor(animator.animatedValue as Int)
            (buttonShowAllDetails as MaterialButton).icon.setTint(animator.animatedValue as Int)
        }
        colorAnimation.start()
        when (category) {
            School.Category.HOMEWORK -> {
                chipHomework.chipBackgroundColor = ContextCompat.getColorStateList(
                    requireContext(),
                    categoryChipBackgroundColorStateList[category]
                )
                chipHomework.isSelected = true

            }
            School.Category.EXAM -> {
                chipExam.chipBackgroundColor = ContextCompat.getColorStateList(
                    requireContext(),
                    categoryChipBackgroundColorStateList[category]
                )
                chipExam.isSelected = true
            }
            School.Category.TASK -> {
                chipTask.chipBackgroundColor = ContextCompat.getColorStateList(
                    requireContext(),
                    categoryChipBackgroundColorStateList[category]
                )
                chipTask.isSelected = true
            }
        }
        chipPickDate.chipBackgroundColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipBackgroundColorStateList[category]
        )
        chipToday.chipBackgroundColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipBackgroundColorStateList[category]
        )
        chipTomorrow.chipBackgroundColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipBackgroundColorStateList[category]
        )
        chipPickDate.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[category]
        )
        chipToday.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[category]
        )
        chipTomorrow.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[category]
        )
        categoryChangeListener.selectedCategoryChanged(category)
    }

    interface SelectedCategoryChangeListener {
        fun selectedCategoryChanged(category: Int)
    }
}
package com.dan.school

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.android.synthetic.main.fragment_edit.buttonCheck
import kotlinx.android.synthetic.main.fragment_edit.chipGroupDate
import kotlinx.android.synthetic.main.fragment_edit.chipPickDate
import kotlinx.android.synthetic.main.fragment_edit.chipToday
import kotlinx.android.synthetic.main.fragment_edit.chipTomorrow
import kotlinx.android.synthetic.main.fragment_edit.editTextTitle
import kotlinx.android.synthetic.main.fragment_edit.textViewDatePicked
import java.text.SimpleDateFormat
import java.util.*

class EditFragment(
        private val categoryChangeListener: CategoryChangeListener,
        private val listener: DismissBottomSheet,
        private var category: Int = School.Category.HOMEWORK,
        private val title: String = "",
        private val chipGroupSelected: Int = School.Date.TODAY,
        private var selectedDate: Calendar? = Calendar.getInstance()
) : DialogFragment() {

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
    private val dateToday = Calendar.getInstance()
    private val dateTomorrow = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialog
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            listener.dismissBottomSheet()
        }

        val viewPagerAdapter = InfinitePagerAdapter(CategoryViewPagerAdapter(requireContext()))

        viewPagerCategory.adapter = viewPagerAdapter
        viewPagerCategory.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                val realPosition = viewPagerCategory.currentItem
                changeColors(realPosition)
            }
        })

        // listeners
        buttonShowMore.setOnClickListener {
            constraintLayoutMore.visibility = View.VISIBLE
            buttonShowMore.visibility = View.GONE
        }

        // initialize
        dateTomorrow.add(Calendar.DAY_OF_MONTH, 1)
        viewPagerCategory.currentItem += category
        textViewDatePicked.setTextColor(ContextCompat.getColor(requireContext(), categoryColors[category]))
        editTextTitle.setText(title)
        when (chipGroupSelected) {
            School.Date.TODAY -> {
                chipGroupDate.check(R.id.chipToday)
                textViewDatePicked.text = dateFormat.format(dateToday.time)
            }
            School.Date.TOMORROW -> {
                chipGroupDate.check(R.id.chipTomorrow)
                textViewDatePicked.text = dateFormat.format(dateTomorrow.time)
            }
            School.Date.PICK_DATE -> {
                chipGroupDate.check(R.id.chipPickDate)
                if (selectedDate != null) {
                    textViewDatePicked.text = dateFormat.format(selectedDate!!.time)
                }
            }
        }
        changeColors(category)
    }

    private fun changeColors(newCategory: Int) {
        val colorFrom = ContextCompat.getColor(requireContext(), categoryColors[category])
        val colorTo = ContextCompat.getColor(requireContext(), categoryColors[newCategory])
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            textViewDatePicked.setTextColor(animatedValue)
            textViewDatePicked.setTextColor(animatedValue)
        }
        colorAnimation.start()
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
        categoryChangeListener.selectedCategoryChanged(newCategory)
        category = newCategory
    }

    interface DismissBottomSheet {
        fun dismissBottomSheet()
    }

    interface CategoryChangeListener {
        fun selectedCategoryChanged(category: Int)
    }
}
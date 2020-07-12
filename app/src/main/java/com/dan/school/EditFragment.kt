package com.dan.school

import android.animation.ArgbEvaluator
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_edit.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditFragment(
    private val categoryChangeListener: CategoryChangeListener,
    private val listener: DismissBottomSheet,
    private var category: Int = School.Category.HOMEWORK,
    private val title: String = "",
    private val chipGroupSelected: Int = School.Date.TODAY,
    private var selectedDate: Calendar? = Calendar.getInstance()
) : DialogFragment(), SubtaskListAdapter.DataChangeListener, SubtaskListAdapter.SetFocusListener {

    private val categoryColors =
        arrayOf(R.color.homeworkColor, R.color.examColor, R.color.taskColor)
    private val categoryChipBackgroundColorStateList = arrayOf(
        R.color.chip_homework_background_state_list,
        R.color.chip_exam_background_state_list,
        R.color.chip_task_background_state_list
    )
    private val categoryChipStrokeColorStateList = arrayOf(
        R.color.chip_homework_stroke_color_state_list,
        R.color.chip_exam_stroke_color_state_list,
        R.color.chip_task_stroke_color_state_list
    )
    private val categorySwitchThumbColorStateList = arrayOf(
        R.color.switch_thumb_homework_color_state_list,
        R.color.switch_thumb_exam_color_state_list,
        R.color.switch_thumb_task_color_state_list
    )
    private val categorySwitchTrackColorStateList = arrayOf(
        R.color.switch_track_homework_color_state_list,
        R.color.switch_track_exam_color_state_list,
        R.color.switch_track_task_color_state_list
    )
    private val categoryButtonAddColorStateList = arrayOf(
        R.color.button_add_homework_color_state_list,
        R.color.button_add_exam_color_state_list,
        R.color.button_add_task_color_state_list
    )
    private val categoryButtonAddRippleColorStateList = arrayOf(
        R.color.button_add_ripple_homework_color_state_list,
        R.color.button_add_ripple_exam_color_state_list,
        R.color.button_add_ripple_task_color_state_list
    )
    private val categoryCheckedIcons = arrayOf(
        R.drawable.ic_homework_checked,
        R.drawable.ic_exam_checked,
        R.drawable.ic_task_checked
    )
    private val categoryUncheckedIcons = arrayOf(
        R.drawable.ic_homework_unchecked,
        R.drawable.ic_exam_unchecked,
        R.drawable.ic_task_unchecked
    )
    private val dateToday = Calendar.getInstance()
    private val dateTomorrow = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var subtaskListAdapter: SubtaskListAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

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
        buttonAddSubtask.setOnClickListener {
            (recyclerViewSubtasks.adapter as SubtaskListAdapter).data.add(Subtask())
            (recyclerViewSubtasks.adapter as SubtaskListAdapter).notifyItemInserted((recyclerViewSubtasks.adapter as SubtaskListAdapter).itemCount - 1)
            inputMethodManager.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                0
            )
        }

        // initialize
        dateTomorrow.add(Calendar.DAY_OF_MONTH, 1)
        textViewDatePicked.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                categoryColors[category]
            )
        )
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
        subtaskListAdapter = SubtaskListAdapter(
            requireContext(),
            this,
            this,
            categoryCheckedIcons[category],
            categoryUncheckedIcons[category],
            ArrayList()
        )
        recyclerViewSubtasks.layoutManager = LinearLayoutManager(context)
        recyclerViewSubtasks.adapter = subtaskListAdapter
        if (category == School.Category.HOMEWORK) {
            changeColors(category)
        } else {
            viewPagerCategory.currentItem += category
        }
        constraintLayoutMore.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        editTextTitle.requestFocus()
    }

    private fun changeColors(newCategory: Int) {
        val colorFrom = ContextCompat.getColor(requireContext(), categoryColors[category])
        val colorTo = ContextCompat.getColor(requireContext(), categoryColors[newCategory])
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            textViewDatePicked.setTextColor(animatedValue)
            textViewDatePicked.setTextColor(animatedValue)
            buttonPlusReminder.setColorFilter(animatedValue)
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
        chipTomorrow.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[newCategory]
        )
        chipTomorrow.chipStrokeColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryChipStrokeColorStateList[newCategory]
        )
        switchReminder.thumbTintList = ContextCompat.getColorStateList(
            requireContext(),
            categorySwitchThumbColorStateList[newCategory]
        )
        switchReminder.trackTintList = ContextCompat.getColorStateList(
            requireContext(),
            categorySwitchTrackColorStateList[newCategory]
        )
        (buttonAddSubtask as MaterialButton).iconTint = ContextCompat.getColorStateList(
            requireContext(),
            categoryButtonAddColorStateList[newCategory]
        )
        buttonAddSubtask.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                categoryButtonAddColorStateList[newCategory]
            )
        )
        (buttonAddSubtask as MaterialButton).rippleColor = ContextCompat.getColorStateList(
            requireContext(),
            categoryButtonAddRippleColorStateList[newCategory]
        )
        changeSubtaskListColor(newCategory)
        categoryChangeListener.selectedCategoryChanged(newCategory)
        category = newCategory
    }

    private fun changeSubtaskListColor(newCategory: Int) {
        subtaskListAdapter.iconChecked = categoryCheckedIcons[newCategory]
        subtaskListAdapter.iconUnchecked = categoryUncheckedIcons[newCategory]
        for (i in 0 until subtaskListAdapter.itemCount) {
            val v = recyclerViewSubtasks.getChildAt(i)
            val buttonCheck = v.findViewById<ImageButton>(R.id.buttonCheck)
            if (!subtaskListAdapter.data[i].done) {
                buttonCheck.setImageResource(categoryUncheckedIcons[newCategory])
            } else {
                buttonCheck.setImageResource(categoryCheckedIcons[newCategory])
            }
            buttonCheck.setOnClickListener {
                if (subtaskListAdapter.data[i].done) {
                    buttonCheck.setImageResource(categoryUncheckedIcons[newCategory])
                    subtaskListAdapter.data[i].done = false
                } else {
                    buttonCheck.setImageResource(categoryCheckedIcons[newCategory])
                    subtaskListAdapter.data[i].done = true
                }
            }
        }
    }

//    private fun setListViewHeightBasedOnChild(recyclerView: RecyclerView) {
//        val listAdapter = recyclerView.adapter as SubtaskListAdapter
//        val width = View.MeasureSpec.makeMeasureSpec(
//            recyclerView.width,
//            View.MeasureSpec.UNSPECIFIED
//        )
//        var height = 0
//        var mView: View? = null
//        for (i in 0 until listAdapter.itemCount) {
//            mView = listAdapter.(i, mView, recyclerView)
//            if (i == 0) {
//                mView.layoutParams = ViewGroup.LayoutParams(
//                    width,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//                )
//            }
//            mView.measure(width, View.MeasureSpec.UNSPECIFIED)
//            height += mView.measuredHeight
//        }
//        val params = recyclerView.layoutParams
//        params.height = height + recyclerView.dividerHeight * (listAdapter.count - 1)
//        recyclerView.layoutParams = params
//    }

    interface DismissBottomSheet {
        fun dismissBottomSheet()
    }

    interface CategoryChangeListener {
        fun selectedCategoryChanged(category: Int)
    }

    override fun dataChanged() {
//        setListViewHeightBasedOnChild(recyclerViewSubtasks)
        var s = ""
        val data = (recyclerViewSubtasks.adapter as SubtaskListAdapter).data
        for (i in 0 until subtaskListAdapter.itemCount) {
            s += data[i].title
        }
        Log.i("Test", s)
    }

    override fun setFocus(position: Int) {
        (recyclerViewSubtasks.getChildViewHolder(
            recyclerViewSubtasks.getChildAt(position)
        ) as SubtaskListAdapter.SubtaskViewHolder).editTextSubtaskTitle.requestFocus()
    }
}

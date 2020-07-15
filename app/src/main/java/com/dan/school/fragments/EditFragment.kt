package com.dan.school.fragments

import android.animation.ArgbEvaluator
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import com.dan.school.*
import com.dan.school.adapters.CategoryViewPagerAdapter
import com.dan.school.adapters.ReminderListAdapter
import com.dan.school.adapters.SubtaskListAdapter
import com.dan.school.models.Item
import com.dan.school.models.Reminder
import com.dan.school.models.Subtask
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_edit.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditFragment(
    private val categoryChangeListener: CategoryChangeListener,
    private val listener: DismissBottomSheet,
    private var category: Int = School.HOMEWORK,
    private val title: String = "",
    private val chipGroupSelected: Int = School.TODAY,
    private var selectedDate: Calendar?
) : DialogFragment(), SubtaskListAdapter.SetFocusListener,
    DateTimePicker.DoneListener, DatePickerDialog.OnDateSetListener,
    DatePickerFragment.OnCancelListener {

    private val categoryColors =
        arrayOf(
            R.color.homeworkColor,
            R.color.examColor,
            R.color.taskColor
        )
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
    private lateinit var reminderListAdapter: ReminderListAdapter

    private var chipGroupDateSelected: Int = R.id.chipToday

    private lateinit var database: ItemDatabase
    private lateinit var dataViewModel: DataViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations =
            R.style.DialogAnimation
        View.GONE
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
        database = ItemDatabase.getInstance(requireContext())
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
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

        val viewPagerAdapter = InfinitePagerAdapter(
            CategoryViewPagerAdapter(
                requireContext()
            )
        )

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
            subtaskListAdapter.data.add(Subtask())
            subtaskListAdapter.notifyItemInserted(subtaskListAdapter.itemCount - 1)
            inputMethodManager.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                0
            )
        }
        buttonAddReminder.setOnClickListener {
            DateTimePicker(this, childFragmentManager).startGetDateTime()
        }
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            buttonAddReminder.isEnabled = isChecked
            if (isChecked) {
                recyclerViewReminders.visibility = View.VISIBLE
            } else {
                recyclerViewReminders.visibility = View.GONE
            }
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
                title = editTextTitle.text.toString(),
                date = dateFormat.format(selectedDate!!.time),
                subtasks = subtaskListAdapter.data,
                reminders = reminderListAdapter.data,
                notes = editTextNotes.text.toString()
            )
            dataViewModel.insert(item)
        }

        // [START] initialize
        dateTomorrow.add(Calendar.DAY_OF_MONTH, 1)
        textViewDatePicked.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                categoryColors[category]
            )
        )
        editTextTitle.setText(title)
        when (chipGroupSelected) {
            School.TODAY -> {
                chipGroupDate.check(R.id.chipToday)
                textViewDatePicked.text = dateFormat.format(dateToday.time)
                selectedDate = dateToday
            }
            School.TOMORROW -> {
                chipGroupDate.check(R.id.chipTomorrow)
                textViewDatePicked.text = dateFormat.format(dateTomorrow.time)
                selectedDate = dateTomorrow
            }
            School.PICK_DATE -> {
                chipGroupDate.check(R.id.chipPickDate)
                if (selectedDate != null) {
                    textViewDatePicked.text = dateFormat.format(selectedDate!!.time)
                }
            }
        }

        // [START] configure subtasks list
        subtaskListAdapter = SubtaskListAdapter(
            requireContext(),
            this,
            categoryCheckedIcons[category],
            categoryUncheckedIcons[category],
            ArrayList()
        )
        recyclerViewSubtasks.layoutManager = LinearLayoutManager(context)
        recyclerViewSubtasks.adapter = subtaskListAdapter
        // [END] configure subtasks list

        // [START] configure reminders list
        reminderListAdapter = ReminderListAdapter(
            requireContext(),
            ArrayList()
        )
        recyclerViewReminders.layoutManager = LinearLayoutManager(context)
        recyclerViewReminders.adapter = reminderListAdapter
        // [END] configure reminders list

        if (category == School.HOMEWORK) {
            changeColors(category)
        } else {
            viewPagerCategory.currentItem += category
        }
        constraintLayoutMore.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        editTextTitle.requestFocus()
        buttonAddReminder.isEnabled = switchReminder.isChecked
        // [END] initialize
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
        buttonAddReminder.imageTintList = ContextCompat.getColorStateList(
            requireContext(),
            categoryButtonAddColorStateList[newCategory]
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

    interface DismissBottomSheet {
        fun dismissBottomSheet()
    }

    interface CategoryChangeListener {
        fun selectedCategoryChanged(category: Int)
    }

    override fun setFocus(position: Int) {
        if (position == -1) {
            editTextTitle.requestFocus()
        } else {
            (recyclerViewSubtasks.getChildViewHolder(
                recyclerViewSubtasks.getChildAt(position)
            ) as SubtaskListAdapter.SubtaskViewHolder).editTextSubtaskTitle.requestFocus()
        }
    }

    override fun done(calendar: Calendar) {
        reminderListAdapter.data.add(
            0,
            Reminder(
                SimpleDateFormat(
                    School.dateTimeFormat,
                    Locale.getDefault()
                ).format(calendar.time)
            )
        )
        reminderListAdapter.notifyItemInserted(0)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (selectedDate == null) {
            selectedDate = Calendar.getInstance()
        }
        selectedDate!!.set(Calendar.YEAR, year)
        selectedDate!!.set(Calendar.MONTH, month)
        selectedDate!!.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        textViewDatePicked.text = dateFormat.format(selectedDate!!.time)
        chipGroupDateSelected = R.id.chipPickDate
    }

    override fun canceled() {
        chipGroupDate.check(chipGroupDateSelected)
    }
}

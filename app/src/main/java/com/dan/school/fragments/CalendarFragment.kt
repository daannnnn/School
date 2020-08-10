package com.dan.school.fragments

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.*
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.models.CategoryCount
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList

class CalendarFragment : Fragment(), ItemListAdapter.DoneListener,
    ItemListAdapter.ShowSubtasksListener, ItemClickListener, ItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialog.ConfirmDeleteListener {

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

    private lateinit var titleChangeListener: TitleChangeListener
    private lateinit var itemClickListener: ItemClickListener

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private lateinit var dataViewModel: DataViewModel
    private lateinit var lastMonth: YearMonth
    private val events = mutableMapOf<LocalDate, CategoryCount>()

    private val titleMonthFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleMonthWithYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    private lateinit var displayMetrics: DisplayMetrics
    private lateinit var dayView: View

    private var calendarScrolled = false

    private lateinit var allHomeworks: ArrayList<Date>
    private lateinit var allExams: ArrayList<Date>
    private lateinit var allTasks: ArrayList<Date>

    private var selectedDateChanged = arrayOf(true, true, true)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OverviewFragment) {
            titleChangeListener = (parentFragment as OverviewFragment)
            itemClickListener = (parentFragment as OverviewFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [START] CalendarView setup
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val textViewCalendarDay = view.findViewById<TextView>(R.id.textViewCalendarDay)
            val imageViewIndicator = view.findViewById<ImageView>(R.id.imageViewIndicator)
            val viewHomeworkDotIndicator = view.findViewById<View>(R.id.viewHomeworkDotIndicator)
            val viewExamDotIndicator = view.findViewById<View>(R.id.viewExamDotIndicator)
            val viewTaskDotIndicator = view.findViewById<View>(R.id.viewTaskDotIndicator)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    } else {
                        calendarScrolled = false
                        selectDate(day.date, true)
                    }
                }
            }
        }

        val typedValue = TypedValue()
        val theme: Resources.Theme = requireContext().theme
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textViewCalendarDay.text = day.date.dayOfMonth.toString()
                container.day = day

                if (day.owner == DayOwner.THIS_MONTH) {
                    when (day.date) {
                        today -> {
                            if (selectedDate == today) {
                                container.imageViewIndicator.setBackgroundResource(R.drawable.date_selected_background)
                                container.textViewCalendarDay.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        android.R.color.white
                                    )
                                )
                            } else {
                                container.imageViewIndicator.background = null
                                container.textViewCalendarDay.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.colorPrimary
                                    )
                                )
                            }
                        }
                        selectedDate -> {
                            container.textViewCalendarDay.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    android.R.color.white
                                )
                            )
                            container.imageViewIndicator.setBackgroundResource(R.drawable.date_selected_background)
                        }
                        else -> {
                            val textColorPrimary: TypedArray = requireContext().obtainStyledAttributes(
                                typedValue.data, intArrayOf(
                                    android.R.attr.textColorPrimary
                                )
                            )
                            container.textViewCalendarDay.setTextColor(textColorPrimary.getColor(0, -1))
                            textColorPrimary.recycle()
                            container.imageViewIndicator.background = null
                        }
                    }
                } else {
                    val textColorSecondary: TypedArray = requireContext().obtainStyledAttributes(
                        typedValue.data, intArrayOf(
                            android.R.attr.textColorSecondary
                        )
                    )
                    container.textViewCalendarDay.setTextColor(textColorSecondary.getColor(0, -1))
                    textColorSecondary.recycle()
                    container.imageViewIndicator.background = null
                }
                if (events[day.date] != null) {
                    container.viewHomeworkDotIndicator.isVisible =
                        events[day.date]!!.homeworkCount != 0
                    container.viewExamDotIndicator.isVisible =
                        events[day.date]!!.examCount != 0
                    container.viewTaskDotIndicator.isVisible =
                        events[day.date]!!.taskCount != 0
                } else {
                    container.viewHomeworkDotIndicator.isVisible = false
                    container.viewExamDotIndicator.isVisible = false
                    container.viewTaskDotIndicator.isVisible = false
                }
            }
        }

        calendarView.monthScrollListener = {
            if (calendarView.maxRowCount == 6) {
                if (this::titleChangeListener.isInitialized) {
                    titleChangeListener.changeTitle(
                        if (it.year == today.year) {
                            titleMonthFormatter.format(it.yearMonth)
                        } else {
                            titleMonthWithYearFormatter.format(it.yearMonth)
                        }
                    )
                }
                if (calendarScrolled) {
                    selectDate(it.yearMonth.atDay(1))
                } else {
                    if (selectedDate == null) {
                        selectDate()
                    } else {
                        selectDate(selectedDate!!)
                    }
                    calendarScrolled = true
                }
            } else {
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    if (this::titleChangeListener.isInitialized) {
                        titleChangeListener.changeTitle(titleMonthFormatter.format(firstDate))
                    }
                } else {
                    if (this::titleChangeListener.isInitialized) {
                        titleChangeListener.changeTitle(
                            "${titleMonthFormatter.format(firstDate)} - ${titleMonthFormatter.format(
                                lastDate
                            )}"
                        )
                    }
                }
            }
        }

        // set calendar dayHeight and dayWidth
        displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        dayView = View.inflate(context, R.layout.layout_calendar_day, null)
        val widthMeasureSpec =
            MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.AT_MOST)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        dayView.measure(widthMeasureSpec, heightMeasureSpec)
        calendarView.dayHeight = dayView.measuredHeight
        calendarView.dayWidth = ((displayMetrics.widthPixels / 7f) + 0.5).toInt()

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        // [END] CalendarView setup

        dataViewModel.homeworkAllDates.observe(viewLifecycleOwner, Observer { dateItems ->
            if (this::allHomeworks.isInitialized) {
                val addedData = ArrayList<Date>(dateItems)
                val removedData = ArrayList<Date>(allHomeworks)

                addedData.removeAll(ArrayList<Date>(allHomeworks))
                removedData.removeAll(ArrayList<Date>(dateItems))

                dataUpdated(
                    dateItems,
                    addedData,
                    removedData,
                    School.HOMEWORK
                )
            } else {
                initializeData(School.HOMEWORK, dateItems)
            }

        })

        dataViewModel.examAllDates.observe(viewLifecycleOwner, Observer { dateItems ->
            if (this::allExams.isInitialized) {
                val addedData = ArrayList<Date>(dateItems)
                val removedData = ArrayList<Date>(allExams)

                addedData.removeAll(ArrayList<Date>(allExams))
                removedData.removeAll(ArrayList<Date>(dateItems))

                dataUpdated(
                    dateItems,
                    addedData,
                    removedData,
                    School.EXAM
                )
            } else {
                initializeData(School.EXAM, dateItems)
            }
        })

        dataViewModel.taskAllDates.observe(viewLifecycleOwner, Observer { dateItems ->
            if (this::allTasks.isInitialized) {
                val addedData = ArrayList<Date>(dateItems)
                val removedData = ArrayList<Date>(allTasks)

                addedData.removeAll(ArrayList<Date>(allTasks))
                removedData.removeAll(ArrayList<Date>(dateItems))

                dataUpdated(
                    dateItems,
                    addedData,
                    removedData,
                    School.TASK
                )
            } else {
                initializeData(School.TASK, dateItems)
            }
        })

        recyclerViewCalendarHomework.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ItemListAdapter(
                requireContext(),
                this@CalendarFragment,
                this@CalendarFragment,
                this@CalendarFragment,
                this@CalendarFragment
            )
        }

        recyclerViewCalendarExam.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ItemListAdapter(
                requireContext(),
                this@CalendarFragment,
                this@CalendarFragment,
                this@CalendarFragment,
                this@CalendarFragment
            )
        }

        recyclerViewCalendarTask.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ItemListAdapter(
                requireContext(),
                this@CalendarFragment,
                this@CalendarFragment,
                this@CalendarFragment,
                this@CalendarFragment
            )
        }

        dataViewModel.getCalendarHomeworks().observe(viewLifecycleOwner, Observer {
            if (selectedDateChanged[School.HOMEWORK]) {
                recyclerViewCalendarHomework.adapter = ItemListAdapter(
                    requireContext(),
                    this,
                    this,
                    this,
                    this
                )
                selectedDateChanged[School.HOMEWORK] = false
            }
            (recyclerViewCalendarHomework.adapter as ItemListAdapter).submitList(it)
            groupHomework.isGone = it.isEmpty()
        })
        dataViewModel.getCalendarExams().observe(viewLifecycleOwner, Observer {
            if (selectedDateChanged[School.EXAM]) {
                recyclerViewCalendarExam.adapter = ItemListAdapter(
                    requireContext(),
                    this,
                    this,
                    this,
                    this
                )
                selectedDateChanged[School.EXAM] = false
            }
            (recyclerViewCalendarExam.adapter as ItemListAdapter).submitList(it)
            groupExam.isGone = it.isEmpty()
        })
        dataViewModel.getCalendarTasks().observe(viewLifecycleOwner, Observer {
            if (selectedDateChanged[School.TASK]) {
                recyclerViewCalendarTask.adapter = ItemListAdapter(
                    requireContext(),
                    this,
                    this,
                    this,
                    this
                )
                selectedDateChanged[School.TASK] = false
            }
            (recyclerViewCalendarTask.adapter as ItemListAdapter).submitList(it)
            groupTask.isGone = it.isEmpty()
        })
    }

    private fun dataUpdated(
        newItems: List<Date>,
        addedData: List<Date>,
        removedData: List<Date>,
        category: Int
    ) {
        if (addedData.isNotEmpty()) {
            for (data in addedData) {
                val date =
                    data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                if (events[date] == null) {
                    events[date] = CategoryCount()
                }
                when (category) {
                    School.HOMEWORK -> {
                        if (++events[date]!!.homeworkCount == 1) {
                            calendarView.notifyDateChanged(date)
                        }
                    }
                    School.EXAM -> {
                        if (++events[date]!!.examCount == 1) {
                            calendarView.notifyDateChanged(date)
                        }
                    }
                    School.TASK -> {
                        if (++events[date]!!.taskCount == 1) {
                            calendarView.notifyDateChanged(date)
                        }
                    }
                }
            }
        }
        if (removedData.isNotEmpty()) {
            for (data in removedData) {
                val date =
                    data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                when (category) {
                    School.HOMEWORK -> {
                        if (--events[date]!!.homeworkCount == 0) {
                            calendarView.notifyDateChanged(date)
                        }
                    }
                    School.EXAM -> {
                        if (--events[date]!!.examCount == 0) {
                            calendarView.notifyDateChanged(date)
                        }
                    }
                    School.TASK -> {
                        if (--events[date]!!.taskCount == 0) {
                            calendarView.notifyDateChanged(date)
                        }
                    }
                }
            }
        }
        when (category) {
            School.HOMEWORK -> allHomeworks = ArrayList(newItems)
            School.EXAM -> allExams = ArrayList(newItems)
            School.TASK -> allTasks = ArrayList(newItems)
        }
    }

    private fun initializeData(category: Int, newItems: List<Date>) {
        when (category) {
            School.HOMEWORK -> allHomeworks = ArrayList(newItems)
            School.EXAM -> allExams = ArrayList(newItems)
            School.TASK -> allTasks = ArrayList(newItems)
        }
        for (item in newItems) {
            val date = item.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            if (events[date] == null) {
                events[date] = CategoryCount()
            }
            when (category) {
                School.HOMEWORK -> {
                    events[date]!!.homeworkCount++
                }
                School.EXAM -> {
                    events[date]!!.examCount++
                }
                School.TASK -> {
                    events[date]!!.taskCount++
                }
            }
        }
        calendarView.notifyCalendarChanged()
    }

    fun selectDate(date: LocalDate = LocalDate.now(), scrollToMonth: Boolean = false) {
        if (selectedDate == null) {
            selectedDate = LocalDate.now()
            calendarView.notifyDateChanged(date)
        } else {
            if (selectedDate != date) {
                if (scrollToMonth) {
                    calendarView.scrollToMonth(date.yearMonth)
                }
                val oldDate = selectedDate
                selectedDate = date
                calendarView.notifyDateChanged(date)
                oldDate?.let { it -> calendarView.notifyDateChanged(it) }
            }
        }
        selectedDateChanged = arrayOf(true, true, true)
        dataViewModel.setCalendarSelectedDate(
            SimpleDateFormat(
                School.dateFormatOnDatabase,
                Locale.getDefault()
            ).format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .toInt()
        )
    }

    interface TitleChangeListener {
        fun changeTitle(title: String)
    }

    fun getSelectedMonth(): String {
        val month = if (selectedDate != null) {
            selectedDate?.yearMonth
        } else {
            LocalDate.now().yearMonth
        }
        return if (month?.year == today.year) {
            titleMonthFormatter.format(month)
        } else {
            titleMonthWithYearFormatter.format(month)
        }
    }

    fun setCalendarView(isMonthView: Boolean) {
        val oneWeekHeight = calendarView.dayHeight
        val oneMonthHeight = oneWeekHeight * 6

        val oldHeight = if (isMonthView) oneMonthHeight else oneWeekHeight
        val newHeight = if (isMonthView) oneWeekHeight else oneMonthHeight

        val animator = ValueAnimator.ofInt(oldHeight, newHeight)
        animator.addUpdateListener { mAnimator ->
            calendarView.updateLayoutParams {
                height = mAnimator.animatedValue as Int
            }
        }

        animator.doOnStart {
            if (!isMonthView) {
                calendarView.apply {
                    maxRowCount = 6
                    hasBoundaries = true
                }
            }
        }
        animator.doOnEnd {
            if (isMonthView) {
                calendarView.apply {
                    maxRowCount = 1
                    hasBoundaries = false
                }
            }

            if (isMonthView) {
                calendarView.scrollToDate(selectedDate!!)
            } else {
                calendarScrolled = false
                calendarView.scrollToMonth(selectedDate!!.yearMonth)
            }
        }
        animator.duration = 250
        animator.start()
    }

    override fun setDone(id: Int, done: Boolean, doneTime: Long?) {
        dataViewModel.setDone(id, done, doneTime)
    }

    override fun showSubtasks(
        subtasks: ArrayList<Subtask>,
        itemTitle: String,
        id: Int,
        category: Int
    ) {
        SubtasksBottomSheetDialogFragment(
            subtasks,
            itemTitle,
            id,
            categoryUncheckedIcons[category],
            categoryCheckedIcons[category]
        ).show(
            childFragmentManager,
            "subtasksBottomSheet"
        )
    }

    override fun itemClicked(item: Item) {
        if (this::itemClickListener.isInitialized) {
            itemClickListener.itemClicked(item)
        }
    }

    override fun itemLongClicked(title: String, id: Int) {
        ConfirmDeleteDialog(this, id, title).show(childFragmentManager, "confirmDeleteDialog")
    }

    override fun confirmDelete(itemId: Int) {
        dataViewModel.deleteItemWithId(itemId)
    }
}
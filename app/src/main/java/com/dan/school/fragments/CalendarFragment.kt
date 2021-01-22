package com.dan.school.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Bundle
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.DataViewModel
import com.dan.school.ItemClickListener
import com.dan.school.R
import com.dan.school.School
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryUncheckedIcons
import com.dan.school.adapters.BaseItemListAdapter
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.models.CategoryCount
import com.dan.school.models.DateItem
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.Size
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

class CalendarFragment : Fragment(), BaseItemListAdapter.DoneListener,
    BaseItemListAdapter.ShowSubtasksListener, ItemClickListener, BaseItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private lateinit var titleChangeListener: TitleChangeListener
    private lateinit var itemClickListener: ItemClickListener

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val dataViewModel: DataViewModel by activityViewModels()

    private val events = mutableMapOf<LocalDate, CategoryCount>()

    private val titleMonthFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleMonthWithYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    private lateinit var dayView: View

    // Used to compare old and new data after updating
    private lateinit var allHomeworks: ArrayList<DateItem>
    private lateinit var allExams: ArrayList<DateItem>
    private lateinit var allTasks: ArrayList<DateItem>

    private var isViewChangeDone: Boolean = true

    private var selectedDateChanged = arrayOf(true, true, true)

    /**
     * Set to true if [calendarView] is scrolled without
     * selecting a date, false otherwise.
     *
     * If set to true, the first day of the month
     * after scrolling will be selected, the date selected
     * otherwise.
     */
    private var calendarScrolled = false

    /**
     * Used to check if there are no items for all
     * categories on [selectedDate]
     */
    private var isEmpty = arrayOf(false, false, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OverviewFragment) {
            titleChangeListener = (parentFragment as OverviewFragment)
            itemClickListener = (parentFragment as OverviewFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            selectedDate = Date(savedInstanceState.getLong(School.SELECTED_DATE)).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate()
        }
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
                            val textColorPrimary: TypedArray =
                                requireContext().obtainStyledAttributes(
                                    TypedValue().data, intArrayOf(
                                        android.R.attr.textColorPrimary
                                    )
                                )
                            container.textViewCalendarDay.setTextColor(
                                textColorPrimary.getColor(
                                    0,
                                    -1
                                )
                            )
                            textColorPrimary.recycle()
                            container.imageViewIndicator.background = null
                        }
                    }
                } else {
                    val textColorSecondary: TypedArray = requireContext().obtainStyledAttributes(
                        TypedValue().data, intArrayOf(
                            android.R.attr.textColorSecondary
                        )
                    )
                    container.textViewCalendarDay.setTextColor(textColorSecondary.getColor(0, -1))
                    textColorSecondary.recycle()
                    container.imageViewIndicator.background = null
                }

                /**
                 * Show category indicator if the count in the
                 * category is not equal to 0
                 */
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
            if (calendarView.maxRowCount == 6) {  // If in month view
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
            } else {  // If in week view
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    if (this::titleChangeListener.isInitialized) {
                        titleChangeListener.changeTitle(titleMonthFormatter.format(firstDate))
                    }
                } else {
                    if (this::titleChangeListener.isInitialized) {
                        titleChangeListener.changeTitle(
                            "${titleMonthFormatter.format(firstDate)} - ${
                                titleMonthFormatter.format(
                                    lastDate
                                )
                            }"
                        )
                    }
                }
            }
        }

        // set calendar dayHeight and dayWidth
        val widthPixels = Resources.getSystem().displayMetrics.widthPixels
        dayView = View.inflate(context, R.layout.layout_calendar_day, null)
        val widthMeasureSpec =
            MeasureSpec.makeMeasureSpec(widthPixels, MeasureSpec.AT_MOST)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        dayView.measure(widthMeasureSpec, heightMeasureSpec)
        calendarView.daySize =
            Size(((widthPixels / 7f) + 0.5).toInt(), dayView.measuredHeight)

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        // [END] CalendarView setup

        dataViewModel.homeworkAllDates.observe(viewLifecycleOwner, { dateItems ->
            if (this::allHomeworks.isInitialized) {
                val addedData = ArrayList<DateItem>(dateItems)
                val removedData = ArrayList<DateItem>(allHomeworks)

                addedData.removeAll(ArrayList<DateItem>(allHomeworks))
                removedData.removeAll(ArrayList<DateItem>(dateItems))

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

        dataViewModel.examAllDates.observe(viewLifecycleOwner, { dateItems ->
            if (this::allExams.isInitialized) {
                val addedData = ArrayList<DateItem>(dateItems)
                val removedData = ArrayList<DateItem>(allExams)

                addedData.removeAll(ArrayList<DateItem>(allExams))
                removedData.removeAll(ArrayList<DateItem>(dateItems))

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

        dataViewModel.taskAllDates.observe(viewLifecycleOwner, { dateItems ->
            if (this::allTasks.isInitialized) {
                val addedData = ArrayList<DateItem>(dateItems)
                val removedData = ArrayList<DateItem>(allTasks)

                addedData.removeAll(ArrayList<DateItem>(allTasks))
                removedData.removeAll(ArrayList<DateItem>(dateItems))

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

        /** Observers for the items on [selectedDate] */
        dataViewModel.getCalendarHomeworks().observe(viewLifecycleOwner, {
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
            isEmpty[School.HOMEWORK] = it.isEmpty()
            showNoItemsTextIfAllEmpty()
        })
        dataViewModel.getCalendarExams().observe(viewLifecycleOwner, {
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
            isEmpty[School.EXAM] = it.isEmpty()
            showNoItemsTextIfAllEmpty()
        })
        dataViewModel.getCalendarTasks().observe(viewLifecycleOwner, {
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
            isEmpty[School.TASK] = it.isEmpty()
            showNoItemsTextIfAllEmpty()
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(
            School.SELECTED_DATE,
            Date.from(
                (if (selectedDate != null) selectedDate else LocalDate.now())!!.atStartOfDay(
                    ZoneId.systemDefault()
                )?.toInstant()
            ).time
        )
        super.onSaveInstanceState(outState)
    }

    /**
     * Updates indicators on [calendarView] dates and sets new
     * updated data, [newItems], on one of [allHomeworks], [allExams] or
     * [allTasks]
     *
     * [newItems] are saved to [allHomeworks],
     * [allExams] or [allTasks] depending on the given
     * [category] to be used for comparing new and updated
     * data after data updates
     *
     * [addedData] are added items from the comparison of
     * old and updated data
     *
     * [removedData] are removed items from the comparison
     * of old and updated data
     *
     * [category] is the category of the data updated, one of
     * [School.HOMEWORK], [School.EXAM] or [School.TASK]
     */
    private fun dataUpdated(
        newItems: List<DateItem>,
        addedData: List<DateItem>,
        removedData: List<DateItem>,
        category: Int
    ) {
        if (addedData.isNotEmpty()) {
            for (data in addedData) {
                val date =
                    data.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
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
                    data.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

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

    /** Initializes [calendarView] date indicators */
    private fun initializeData(category: Int, newItems: List<DateItem>) {
        when (category) {
            School.HOMEWORK -> allHomeworks = ArrayList(newItems)
            School.EXAM -> allExams = ArrayList(newItems)
            School.TASK -> allTasks = ArrayList(newItems)
        }
        for (item in newItems) {
            val date = item.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
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

    /**
     * Selects date and updates the recycler view with items
     * on [date]
     *
     * [date] is the selected date
     * [scrollToMonth] is true if the selected date is not
     * in the current month
     */
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
        isEmpty = arrayOf(false, false, false)
        dataViewModel.setCalendarSelectedDate(
            SimpleDateFormat(
                School.dateFormatOnDatabase,
                Locale.getDefault()
            ).format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .toInt()
        )
    }

    /**
     * Returns the selected month in string by formatting
     * it using [titleMonthFormatter] or [titleMonthWithYearFormatter]
     */
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

    /**
     * Sets [calendarView] to month view or
     * week view depending on [isMonthView] if
     * [isViewChangeDone] then returns the value
     * of [isViewChangeDone]
     */
    fun setCalendarView(isMonthView: Boolean): Boolean {
        val b = isViewChangeDone
        if (isViewChangeDone) {
            isViewChangeDone = false

            val oneWeekHeight = calendarView.daySize.height
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

                isViewChangeDone = true
            }
            animator.duration = 250
            animator.start()
        }
        return b
    }

    /**
     * Show [textViewNoItemsForThisDate] and hide
     * [scrollViewCalendarItems] if all items on
     * [isEmpty] are true
     */
    private fun showNoItemsTextIfAllEmpty() {
        textViewNoItemsForThisDate.isVisible =
            (isEmpty[School.HOMEWORK] && isEmpty[School.EXAM] && isEmpty[School.TASK])
        scrollViewCalendarItems.isGone =
            (isEmpty[School.HOMEWORK] && isEmpty[School.EXAM] && isEmpty[School.TASK])
    }

    /**
     * Returns a [Calendar] object from [selectedDate]
     */
    fun getSelectedDate(): Calendar {
        return Calendar.getInstance().apply {
            time = Date.from(selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant())
        }
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
            null
        )
    }

    override fun itemClicked(item: Item) {
        if (this::itemClickListener.isInitialized) {
            itemClickListener.itemClicked(item)
        }
    }

    override fun itemLongClicked(title: String, id: Int) {
        ConfirmDeleteDialogFragment(this, id, title)
            .show(childFragmentManager, null)
    }

    override fun confirmDelete(itemId: Int) {
        dataViewModel.deleteItemWithId(itemId)
    }

    interface TitleChangeListener {
        fun changeTitle(title: String)
    }
}
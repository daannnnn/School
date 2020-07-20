package com.dan.school.fragments

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.DataViewModel
import com.dan.school.R
import com.dan.school.School
import com.dan.school.adapters.ParentEventListAdapter
import com.dan.school.models.Event
import com.dan.school.models.EventList
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CalendarFragment(private val titleChangeListener: TitleChangeListener) : Fragment() {

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private lateinit var dataViewModel: DataViewModel
    private val events = mutableMapOf<LocalDate, EventList>()

    private val titleMonthFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleMonthWithYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    private var setSelectedToFirstDay = true

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
                        selectDate(day.date, false)
                    } else {
                        setSelectedToFirstDay = false
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
                            container.textViewCalendarDay.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    android.R.color.black
                                )
                            )
                            container.imageViewIndicator.background = null
                        }
                    }
                } else {
                    container.textViewCalendarDay.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.darker_gray
                        )
                    )
                    container.imageViewIndicator.background = null
                }
                if (events[day.date] != null) {
                    container.viewHomeworkDotIndicator.isVisible =
                        events[day.date]!!.hasCategory(School.HOMEWORK)
                    container.viewExamDotIndicator.isVisible =
                        events[day.date]!!.hasCategory(School.EXAM)
                    container.viewTaskDotIndicator.isVisible =
                        events[day.date]!!.hasCategory(School.TASK)
                }
            }
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<ViewContainer> {
            override fun create(view: View) = ViewContainer(view)
            override fun bind(container: ViewContainer, month: CalendarMonth) {}
        }

        calendarView.monthScrollListener = {
            titleChangeListener.changeTitle(
                if (it.year == today.year) {
                    titleMonthFormatter.format(it.yearMonth)
                } else {
                    titleMonthWithYearFormatter.format(it.yearMonth)
                }
            )
            if (setSelectedToFirstDay) {
                selectDate(it.yearMonth.atDay(1), false)
            } else {
                setSelectedToFirstDay = true
            }
        }

        // set calendar dayHeight and dayWidth
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val dayView = View.inflate(context, R.layout.layout_calendar_day, null)
        val widthMeasureSpec =
            MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.AT_MOST)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        dayView.measure(widthMeasureSpec, heightMeasureSpec)
        calendarView.dayHeight = dayView.measuredHeight
        calendarView.dayWidth = ((displayMetrics.widthPixels / 7f) + 0.5).toInt()

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        // [END] CalendarView setup

        dataViewModel.homeworkAllDates.observe(viewLifecycleOwner, Observer { dateItems ->
            for (dateItem in dateItems) {
                val localDate =
                    dateItem.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val event = Event(dateItem.id, dateItem.title, School.HOMEWORK, dateItem.done)
                addEventToDate(localDate, event)
                if (selectedDate == localDate) {
                    recyclerViewEventsParent.adapter = ParentEventListAdapter(events[localDate]?.getCategorySortedList()!!, requireContext())
                }
            }
        })

        dataViewModel.examAllDates.observe(viewLifecycleOwner, Observer { dateItems ->
            for (dateItem in dateItems) {
                val localDate =
                    dateItem.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val event = Event(dateItem.id, dateItem.title, School.EXAM, dateItem.done)
                addEventToDate(localDate, event)
                if (selectedDate == localDate) {
                    recyclerViewEventsParent.adapter = ParentEventListAdapter(events[localDate]?.getCategorySortedList()!!, requireContext())
                }
            }
        })

        dataViewModel.taskAllDates.observe(viewLifecycleOwner, Observer { dateItems ->
            for (dateItem in dateItems) {
                val localDate =
                    dateItem.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val event = Event(dateItem.id, dateItem.title, School.TASK, dateItem.done)
                addEventToDate(localDate, event)
                if (selectedDate == localDate) {
                    recyclerViewEventsParent.adapter = ParentEventListAdapter(events[localDate]?.getCategorySortedList()!!, requireContext())
                }
            }
        })

        recyclerViewEventsParent.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun addEventToDate(localDate: LocalDate, event: Event) {
        if (events[localDate] == null) {
            events[localDate] = EventList()
        }
        if (!events[localDate]!!.contains(event)) {
            events[localDate]!!.add(event)
            calendarView.notifyDateChanged(localDate)
        }
    }

    fun selectDate(date: LocalDate, scrollToMonth: Boolean) {
        if (selectedDate == null) {
            selectedDate = LocalDate.now()
            calendarView.notifyDateChanged(date)
            if (events[date] != null) {
                recyclerViewEventsParent.adapter =
                    ParentEventListAdapter(events[date]?.getCategorySortedList()!!, requireContext())
            }
        } else {
            if (selectedDate != date) {
                if (scrollToMonth) {
                    calendarView.scrollToMonth(date.yearMonth)
                }
                val oldDate = selectedDate
                selectedDate = date
                calendarView.notifyDateChanged(date)
                oldDate?.let { it -> calendarView.notifyDateChanged(it) }
                if (events[date] != null) {
                    recyclerViewEventsParent.adapter =
                        ParentEventListAdapter(events[date]?.getCategorySortedList()!!, requireContext())
                }
            }
        }
    }

    interface TitleChangeListener {
        fun changeTitle(title: String)
    }

    fun getSelectedMonth(): String {
        val month = selectedDate?.yearMonth
        return if (month?.year == today.year) {
            titleMonthFormatter.format(month)
        } else {
            titleMonthWithYearFormatter.format(month)
        }
    }
}
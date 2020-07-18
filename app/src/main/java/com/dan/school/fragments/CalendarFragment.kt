package com.dan.school.fragments

import android.R.attr.textSize
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*


class CalendarFragment : Fragment() {

    private var selectedDate = LocalDate.now()
    private val today = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val textViewCalendarDay = view.findViewById<TextView>(R.id.textViewCalendarDay)
            val imageViewIndicator = view.findViewById<ImageView>(R.id.imageViewIndicator)
            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            calendarView.notifyDateChanged(day.date)
                            oldDate?.let { it1 -> calendarView.notifyDateChanged(it1) }
                        }
                    }
                }
            }
        }

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        val dayView = View.inflate(context, R.layout.layout_calendar_day, null);
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.AT_MOST)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        dayView.measure(widthMeasureSpec, heightMeasureSpec)
        calendarView.dayHeight = dayView.measuredHeight
        calendarView.dayWidth = ((displayMetrics.widthPixels/7f)+0.5).toInt()

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
                                container.textViewCalendarDay.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                            } else {
                                container.imageViewIndicator.background = null
                                container.textViewCalendarDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                            }
                        }
                        selectedDate -> {
                            container.textViewCalendarDay.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                            container.imageViewIndicator.setBackgroundResource(R.drawable.date_selected_background)
                        }
                        else -> {
                            container.textViewCalendarDay.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                            container.imageViewIndicator.background = null
                        }
                    }
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.findViewById<LinearLayout>(R.id.legendLayout)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {}
        }


        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
    }
}
package com.dan.school

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.util.*

class DateTimePicker(
    private val doneListener: DoneListener,
    private val childFragmentManager: FragmentManager
) : DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    val calendar: Calendar = Calendar.getInstance()

    fun startGetDateTime() {
        val datePicker: DialogFragment = DatePickerFragment(this, null)
        datePicker.show(childFragmentManager, "date picker")
    }

    interface DoneListener {
        fun done(calendar: Calendar)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val timePicker: DialogFragment = TimePickerFragment(this)
        timePicker.show(childFragmentManager, "time picker")
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        doneListener.done(calendar)
    }
}
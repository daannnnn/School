package com.dan.school.models

import com.dan.school.other.School
import java.text.SimpleDateFormat
import java.util.*

class UpcomingDate(val date: Int): UpcomingListItem() {
    override val type: Int
        get() = TYPE_DATE

    fun getDateString(): String {
        val d = SimpleDateFormat(
            School.dateFormatOnDatabase,
            Locale.getDefault()
        ).parse(date.toString())!!
        return SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(d)
    }
}
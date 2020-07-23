package com.dan.school.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import com.dan.school.School
import kotlinx.android.synthetic.main.fragment_agenda.*
import java.text.SimpleDateFormat
import java.util.*

class AgendaFragment : Fragment() {

    val dateToday = Calendar.getInstance()
    val hourOfDay = dateToday.get(Calendar.HOUR_OF_DAY)
    val userName = "Dan"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewGreeting.text = when (hourOfDay) {
            in 5..11 -> {
                "Good Morning, $userName!"
            }
            in 12..16 -> {
                "Good Afternoon, $userName!"
            }
            else -> {
                "Good Evening, $userName!"
            }
        }

        textViewDate.text = SimpleDateFormat(School.displayDateFormat, Locale.getDefault()).format(dateToday.time)
    }
}
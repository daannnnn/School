package com.dan.school

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavigationUI.setupWithNavController(
            bottom_navigation,
            childFragmentManager
                .findFragmentById(R.id.bottom_navigation_fragment)!!.findNavController()
        )

        fab_menu_homework.setOnClickListener {
            Toast.makeText(context, "Homework Clicked!", Toast.LENGTH_SHORT).show()
        }

        fab_menu_exam.setOnClickListener {
            Toast.makeText(context, "Exam Clicked!", Toast.LENGTH_SHORT).show()
        }

        fab_menu_task.setOnClickListener {
            Toast.makeText(context, "Task Clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.dan.school

import android.animation.Animator
import android.animation.ObjectAnimator
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

    var isFABOpen = false

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

        // Listeners
        fab_menu_homework.setOnClickListener {
            Toast.makeText(context, "Homework Clicked!", Toast.LENGTH_SHORT).show()
        }
        fab_menu_exam.setOnClickListener {
            Toast.makeText(context, "Exam Clicked!", Toast.LENGTH_SHORT).show()
        }
        fab_menu_task.setOnClickListener {
            Toast.makeText(context, "Task Clicked!", Toast.LENGTH_SHORT).show()
        }
        floatingActionMenu.setOnMenuButtonClickListener {
            clickFAB()
        }
        floatingActionMenu.setOnMenuToggleListener { opened ->
            isFABOpen = opened
        }
        shadowView.setOnClickListener {
            clickFAB()
        }
    }

    private fun clickFAB() {
        if (!isFABOpen) {
            floatingActionMenu.toggle(true)
            shadowView.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(shadowView, "alpha", 0.8f).setDuration(300).start()
            shadowView.isFocusable = true
            shadowView.isClickable = true
        } else {
            floatingActionMenu.toggle(true)
            val animator = ObjectAnimator.ofFloat(shadowView, "alpha", 0f).setDuration(300)
            animator.start()
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    shadowView.visibility = View.GONE
                }
            })
            shadowView.isClickable = false
            shadowView.isFocusable = false
        }
    }
}
package com.dan.school

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(), BottomNavigationView.OnNavigationItemSelectedListener {

    var isFABOpen = false
    private lateinit var mChildFragmentManager: FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mChildFragmentManager = childFragmentManager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        bottom_navigation.setOnNavigationItemSelectedListener(this)

        // Show HomeFragment
        mChildFragmentManager.beginTransaction().add(R.id.frameLayoutBottomNavigation, HomeFragment(), "home").commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.homeFragment -> {
                setFragment("home")
            }
            R.id.calendarFragment -> {
                setFragment("calendar")
            }
            R.id.agendaFragment -> {
                setFragment("agenda")
            }
        }
        return true
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

    private fun setFragment(tag: String) {
        if (tag == "home") {
            if (mChildFragmentManager.findFragmentByTag("home") != null) {
                mChildFragmentManager.beginTransaction().show(mChildFragmentManager.findFragmentByTag("home")!!).commit()
            } else {
                mChildFragmentManager.beginTransaction().add(R.id.frameLayoutBottomNavigation, HomeFragment(), "home").commit()
            }
            if(mChildFragmentManager.findFragmentByTag("calendar") != null){
                mChildFragmentManager.beginTransaction().hide(mChildFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
            if(mChildFragmentManager.findFragmentByTag("agenda") != null){
                mChildFragmentManager.beginTransaction().hide(mChildFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
        } else if (tag == "calendar") {
            if (mChildFragmentManager.findFragmentByTag("calendar") != null) {
                mChildFragmentManager.beginTransaction().show(mChildFragmentManager.findFragmentByTag("calendar")!!).commit()
            } else {
                mChildFragmentManager.beginTransaction().add(R.id.frameLayoutBottomNavigation, CalendarFragment(), "calendar").commit()
            }
            if(mChildFragmentManager.findFragmentByTag("home") != null){
                mChildFragmentManager.beginTransaction().hide(mChildFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if(mChildFragmentManager.findFragmentByTag("agenda") != null){
                mChildFragmentManager.beginTransaction().hide(mChildFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
        } else if (tag == "agenda") {
            if (mChildFragmentManager.findFragmentByTag("agenda") != null) {
                mChildFragmentManager.beginTransaction().show(mChildFragmentManager.findFragmentByTag("agenda")!!).commit()
            } else {
                mChildFragmentManager.beginTransaction().add(R.id.frameLayoutBottomNavigation, AgendaFragment(), "agenda").commit()
            }
            if(mChildFragmentManager.findFragmentByTag("home") != null){
                mChildFragmentManager.beginTransaction().hide(mChildFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if(mChildFragmentManager.findFragmentByTag("calendar") != null){
                mChildFragmentManager.beginTransaction().hide(mChildFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
        }
    }
}
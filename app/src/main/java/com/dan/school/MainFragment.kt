package com.dan.school

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(), BottomNavigationView.OnNavigationItemSelectedListener {

    var isFABOpen = false
    private lateinit var mChildFragmentManager: FragmentManager
    var openDrawerListener: OpenDrawerListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mChildFragmentManager = childFragmentManager
        openDrawerListener = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Listeners
        floatingActionButton.setOnClickListener {
            val addPhotoBottomDialogFragment = AddBottomSheetDialogFragment()
            addPhotoBottomDialogFragment.show(
                childFragmentManager,
                "BottomSheet"
            )
        }
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        buttonMenu.setOnClickListener {
            openDrawerListener?.openDrawer()
        }

        // Show HomeFragment
        mChildFragmentManager.beginTransaction()
            .add(R.id.frameLayoutBottomNavigation, HomeFragment(), "home").commit()
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

    private fun setFragment(tag: String) {
        if (tag == "home") {
            if (mChildFragmentManager.findFragmentByTag("home") != null) {
                mChildFragmentManager.beginTransaction()
                    .show(mChildFragmentManager.findFragmentByTag("home")!!).commit()
            } else {
                mChildFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutBottomNavigation, HomeFragment(), "home").commit()
            }
            if (mChildFragmentManager.findFragmentByTag("calendar") != null) {
                mChildFragmentManager.beginTransaction()
                    .hide(mChildFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
            if (mChildFragmentManager.findFragmentByTag("agenda") != null) {
                mChildFragmentManager.beginTransaction()
                    .hide(mChildFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
        } else if (tag == "calendar") {
            if (mChildFragmentManager.findFragmentByTag("calendar") != null) {
                mChildFragmentManager.beginTransaction()
                    .show(mChildFragmentManager.findFragmentByTag("calendar")!!).commit()
            } else {
                mChildFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutBottomNavigation, CalendarFragment(), "calendar").commit()
            }
            if (mChildFragmentManager.findFragmentByTag("home") != null) {
                mChildFragmentManager.beginTransaction()
                    .hide(mChildFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if (mChildFragmentManager.findFragmentByTag("agenda") != null) {
                mChildFragmentManager.beginTransaction()
                    .hide(mChildFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
        } else if (tag == "agenda") {
            if (mChildFragmentManager.findFragmentByTag("agenda") != null) {
                mChildFragmentManager.beginTransaction()
                    .show(mChildFragmentManager.findFragmentByTag("agenda")!!).commit()
            } else {
                mChildFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutBottomNavigation, AgendaFragment(), "agenda").commit()
            }
            if (mChildFragmentManager.findFragmentByTag("home") != null) {
                mChildFragmentManager.beginTransaction()
                    .hide(mChildFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if (mChildFragmentManager.findFragmentByTag("calendar") != null) {
                mChildFragmentManager.beginTransaction()
                    .hide(mChildFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
        }
    }

    interface OpenDrawerListener {
        fun openDrawer()
    }
}
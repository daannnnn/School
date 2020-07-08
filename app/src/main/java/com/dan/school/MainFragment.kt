package com.dan.school

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(), BottomNavigationView.OnNavigationItemSelectedListener,
    AddBottomSheetDialogFragment.GoToEditFragment, EditFragment.DismissBottomSheet {

    private lateinit var mChildFragmentManager: FragmentManager
    private var openDrawerListener: OpenDrawerListener? = null
    private val addBottomSheetDialogFragment = AddBottomSheetDialogFragment(this)

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
            addBottomSheetDialogFragment.show(
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

    override fun goToEditFragment() {
        val editFragment = EditFragment(this, 0)
        editFragment.show(childFragmentManager, "editFragment")
    }

    override fun dismissBottomSheet() {
        addBottomSheetDialogFragment.dismiss()
    }
}
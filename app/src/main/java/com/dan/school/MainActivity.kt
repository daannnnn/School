package com.dan.school

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    AddBottomSheetDialogFragment.GoToEditFragment,
    EditFragment.DismissBottomSheet, SettingsFragment.OnDismissListener {

    private val addBottomSheetDialogFragment = AddBottomSheetDialogFragment(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView.menu.getItem(0).isChecked = true

        // Listeners
        floatingActionButton.setOnClickListener {
            addBottomSheetDialogFragment.show(
                supportFragmentManager,
                "BottomSheet"
            )
        }
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
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
            return@setOnNavigationItemSelectedListener true
        }
        buttonMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navigationView.setNavigationItemSelectedListener { item ->
            if (!item.isChecked) {
                item.isChecked = true
                drawerLayout.closeDrawers()

                when (item.itemId) {
                    R.id.settings -> {
                        val settingsFragment = SettingsFragment(this)
                        settingsFragment.show(supportFragmentManager, "settingsFragment")
                    }
                }
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            return@setNavigationItemSelectedListener true
        }

        // Show HomeFragment
        supportFragmentManager.beginTransaction()
            .add(R.id.frameLayoutBottomNavigation, HomeFragment(), "home").commit()
    }

    private fun setFragment(tag: String) {
        if (tag == "home") {
            if (supportFragmentManager.findFragmentByTag("home") != null) {
                supportFragmentManager.beginTransaction()
                    .show(supportFragmentManager.findFragmentByTag("home")!!).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutBottomNavigation, HomeFragment(), "home").commit()
            }
            if (supportFragmentManager.findFragmentByTag("calendar") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
            if (supportFragmentManager.findFragmentByTag("agenda") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
        } else if (tag == "calendar") {
            if (supportFragmentManager.findFragmentByTag("calendar") != null) {
                supportFragmentManager.beginTransaction()
                    .show(supportFragmentManager.findFragmentByTag("calendar")!!).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutBottomNavigation, CalendarFragment(), "calendar").commit()
            }
            if (supportFragmentManager.findFragmentByTag("home") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if (supportFragmentManager.findFragmentByTag("agenda") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
        } else if (tag == "agenda") {
            if (supportFragmentManager.findFragmentByTag("agenda") != null) {
                supportFragmentManager.beginTransaction()
                    .show(supportFragmentManager.findFragmentByTag("agenda")!!).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutBottomNavigation, AgendaFragment(), "agenda").commit()
            }
            if (supportFragmentManager.findFragmentByTag("home") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if (supportFragmentManager.findFragmentByTag("calendar") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
        }
    }

    override fun goToEditFragment() {
        val editFragment = EditFragment(this, 0)
        editFragment.show(supportFragmentManager, "editFragment")
    }

    override fun dismissBottomSheet() {
        addBottomSheetDialogFragment.dismiss()
    }

    override fun onDismiss() {
        navigationView.setCheckedItem(R.id.overview)
    }
}
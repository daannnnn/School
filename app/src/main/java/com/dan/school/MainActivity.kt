package com.dan.school

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dan.school.fragments.CompletedFragment
import com.dan.school.fragments.OverviewFragment
import com.dan.school.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OverviewFragment.OpenDrawerListener {

    /**
     * Holds id of the item selected on [navigationView]
     * for [navigationItemSelected] to access after [drawerLayout]
     * is closed
     */
    private var navigationSelectedItemId: Int? = null

    /**
     * Is set to not null on [SettingsFragment.onAttach]
     * Is set to null on [SettingsFragment.onDetach]
     */
    var settingsBackPressedListener: SettingsBackPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {  // to prevent multiple creation of instances
            navigationView.menu.getItem(0).isChecked = true
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutMain,
                    OverviewFragment(), School.OVERVIEW
                ).commit()
        }

        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        setNavigationViewHeaderName(sharedPref.getString(School.FULL_NAME, ""))
        setNavigationViewHeaderEmail(sharedPref.getString(School.EMAIL, ""))

        // listeners
        sharedPref.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == School.FULL_NAME) {
                setNavigationViewHeaderName(sharedPreferences.getString(School.FULL_NAME, ""))
            }
            if (key == School.EMAIL) {
                setNavigationViewHeaderEmail(sharedPreferences.getString(School.EMAIL, ""))
            }
        }
        navigationView.setNavigationItemSelectedListener { item ->
            if (!item.isChecked) {
                item.isChecked = true
                drawerLayout.closeDrawers()

                navigationSelectedItemId = item.itemId
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            return@setNavigationItemSelectedListener true
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                navigationSelectedItemId?.let {
                    navigationItemSelected(it)
                    navigationSelectedItemId = null
                }
            }
        })
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                    showFragment(School.OVERVIEW)
                    navigationView.setCheckedItem(R.id.overview)
                }
            } else {
                when (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name) {
                    School.COMPLETED -> {
                        if (supportFragmentManager.findFragmentByTag(School.COMPLETED) != null) {
                            showFragment(School.COMPLETED)
                            navigationView.setCheckedItem(R.id.completed)
                        }
                        if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                            hideFragment(School.OVERVIEW)
                        }
                        if (supportFragmentManager.findFragmentByTag(School.SETTINGS) != null) {
                            hideFragment(School.SETTINGS)
                        }
                    }
                    School.SETTINGS -> {
                        if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                            hideFragment(School.OVERVIEW)
                        }
                        if (supportFragmentManager.findFragmentByTag(School.COMPLETED) != null) {
                            hideFragment(School.COMPLETED)
                        }
                    }
                }
            }
        }
    }

    /** Sets [R.id.textViewEmail] text to [email] */
    private fun setNavigationViewHeaderEmail(email: String?) {
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.textViewEmail).text = email
    }

    /** Sets [R.id.textViewName] text to [name] */
    private fun setNavigationViewHeaderName(name: String?) {
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.textViewName).text = name
    }

    /**
     * Manages fragments when [navigationView] selected item is changed
     * Called after [drawerLayout] is closed
     */
    private fun navigationItemSelected(itemId: Int) {
        when (itemId) {
            R.id.overview -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                if (supportFragmentManager.backStackEntryCount != 0) {
                    supportFragmentManager.popBackStackImmediate()
                    if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                        showFragment(School.OVERVIEW)
                    }
                }
            }
            R.id.completed -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutMain, CompletedFragment(), School.COMPLETED)
                    .addToBackStack(School.COMPLETED)
                    .commit()
            }
            R.id.settings -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                supportFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutMain, SettingsFragment(), School.SETTINGS)
                    .addToBackStack(School.SETTINGS)
                    .commit()
            }
        }
    }

    /** Hides fragment with tag [tag] */
    private fun hideFragment(tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag)!!.isHidden) {
            return
        }
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    /** Shows fragment with tag [tag] */
    private fun showFragment(tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag)!!.isVisible) {
            return
        }
        supportFragmentManager.beginTransaction()
            .show(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    override fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (settingsBackPressedListener == null) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                if (supportFragmentManager.backStackEntryCount == 1) {
                    supportFragmentManager.popBackStackImmediate()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                } else if (supportFragmentManager
                        .getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 2)
                        .name == School.COMPLETED
                ) {
                    supportFragmentManager.popBackStackImmediate()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            } else super.onBackPressed()
        } else {
            settingsBackPressedListener?.backPressed()
        }
    }

    /** Used for back presses when in [SettingsFragment] */
    interface SettingsBackPressedListener {
        fun backPressed()
    }
}
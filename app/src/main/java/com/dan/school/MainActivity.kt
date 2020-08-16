package com.dan.school

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dan.school.fragments.CompletedFragment
import com.dan.school.fragments.OverviewFragment
import com.dan.school.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OverviewFragment.OpenDrawerListener {

    private var navigationSelectedItemId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigationView.menu.getItem(0).isChecked = true

            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutMain,
                    OverviewFragment(), School.OVERVIEW
                ).commit()
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
    }

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
                if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                    hideFragment(School.OVERVIEW)
                }
            }
            R.id.settings -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                supportFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutMain, SettingsFragment(), School.SETTINGS)
                    .addToBackStack(School.SETTINGS)
                    .commit()
                if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                    hideFragment(School.OVERVIEW)
                }
                if (supportFragmentManager.findFragmentByTag(School.COMPLETED) != null) {
                    hideFragment(School.COMPLETED)
                }
            }
        }
    }

    override fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            if (supportFragmentManager.backStackEntryCount == 1) {
                supportFragmentManager.popBackStackImmediate()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                    showFragment(School.OVERVIEW)
                }
                navigationView.setCheckedItem(R.id.overview)
            } else if (supportFragmentManager
                    .getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 2)
                    .name == School.COMPLETED
            ) {
                supportFragmentManager.popBackStackImmediate()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                if (supportFragmentManager.findFragmentByTag(School.COMPLETED) != null) {
                    showFragment(School.COMPLETED)
                }
                navigationView.setCheckedItem(R.id.completed)
            }
        } else super.onBackPressed()
    }

    private fun hideFragment(tag: String) {
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    private fun showFragment(tag: String) {
        supportFragmentManager.beginTransaction()
            .show(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }
}
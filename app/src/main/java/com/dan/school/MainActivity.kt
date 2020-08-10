package com.dan.school

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dan.school.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    DialogInterface.OnDismissListener, OverviewFragment.OpenDrawerListener {

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

                when (item.itemId) {
                    R.id.overview -> {
                        if (supportFragmentManager.backStackEntryCount == 1) {
                            supportFragmentManager.popBackStackImmediate()
                            if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                                showFragment(School.OVERVIEW)
                            }
                        }
                    }
                    R.id.completed -> {
                        supportFragmentManager.beginTransaction()
                            .add(
                                R.id.frameLayoutMain,
                                CompletedFragment()
                            ).addToBackStack(null).commit()
                        if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                            hideFragment(School.OVERVIEW)
                        }
                    }
                    R.id.settings -> {
                        val settingsFragment =
                            SettingsFragment()
                        settingsFragment.show(supportFragmentManager, "settingsFragment")
                    }
                }
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun hideFragment(tag: String) {
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    private fun showFragment(tag: String) {
        supportFragmentManager.beginTransaction()
            .show(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        navigationView.setCheckedItem(R.id.overview)
    }

    override fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            supportFragmentManager.popBackStackImmediate()
            if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                navigationView.setCheckedItem(R.id.overview)
                showFragment(School.OVERVIEW)
            }
        } else super.onBackPressed()
    }
}
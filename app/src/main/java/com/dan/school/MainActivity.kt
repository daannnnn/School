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
                                supportFragmentManager.beginTransaction()
                                    .show(supportFragmentManager.findFragmentByTag(School.OVERVIEW)!!)
                                    .commit()
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
                            supportFragmentManager.beginTransaction()
                                .hide(supportFragmentManager.findFragmentByTag(School.OVERVIEW)!!)
                                .commit()
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
                supportFragmentManager.beginTransaction()
                    .show(supportFragmentManager.findFragmentByTag(School.OVERVIEW)!!)
                    .commit()
            }
        } else super.onBackPressed()
    }
}
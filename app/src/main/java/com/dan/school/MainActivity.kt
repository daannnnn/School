package com.dan.school

import android.R.attr
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val fragmentManager: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView.setNavigationItemSelectedListener(this)
        navigationView.menu.getItem(0).isChecked = true
        fragmentManager.beginTransaction().add(R.id.frameLayoutContent, MainNavigationFragment(), "main_navigation").commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        when (item.itemId) {
            R.id.mainFragment -> {
                setFragment("main_navigation")
            }
            R.id.settingsFragment -> {
                setFragment("settings")
            }
        }
        drawerLayout.closeDrawers()
        return true
    }
    private fun setFragment(tag: String) {
        if (tag == "main_navigation") {
            if (fragmentManager.findFragmentByTag("main_navigation") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("main_navigation")!!).commit()
            } else {
                fragmentManager.beginTransaction().add(R.id.frameLayoutContent, MainNavigationFragment(), "main_navigation").commit()
            }
            if(fragmentManager.findFragmentByTag("settings") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("settings")!!).commit()
            }
        } else if (tag == "settings") {
            if (fragmentManager.findFragmentByTag("settings") != null) {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("settings")!!).commit()
            } else {
                fragmentManager.beginTransaction().add(R.id.frameLayoutContent, SettingsFragment(), "settings").commit()
            }
            if(fragmentManager.findFragmentByTag("main_navigation") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("main_navigation")!!).commit()
            }
        }
    }
}
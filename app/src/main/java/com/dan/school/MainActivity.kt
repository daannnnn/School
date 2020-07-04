package com.dan.school

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    MainFragment.OpenDrawerListener {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(navigationView, navController)

        navigationView.setNavigationItemSelectedListener(this)

        navigationView.menu.getItem(0).isChecked = true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (!item.isChecked) {
            item.isChecked = true
            drawerLayout.closeDrawers()

            when (item.itemId) {
                R.id.mainFragment -> navController.navigate(R.id.mainFragment)
                R.id.settingsFragment -> navController.navigate(R.id.settingsFragment)
            }
        } else {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        return true
    }

    override fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }
}
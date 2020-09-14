package com.dan.school

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // set saved mode
        val sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        if (sharedPref.getBoolean(School.IS_SETUP_DONE, false)) {
            when (sharedPref.getInt(School.SELECTED_THEME, -1)) {
                School.DARK_MODE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                School.LIGHT_MODE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                School.SYSTEM_DEFAULT -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                with(sharedPref.edit()) {
                    this?.putInt(School.SELECTED_THEME, School.SYSTEM_DEFAULT)
                    this?.commit()
                }
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                with(sharedPref.edit()) {
                    this?.putInt(School.SELECTED_THEME, School.LIGHT_MODE)
                    this?.commit()
                }
            }
        }
    }
}
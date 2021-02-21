package com.dan.school

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.dan.school.other.School

class App : Application() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        if (sharedPref.getBoolean(School.IS_SETUP_DONE, false)) {
            setSavedTheme(sharedPref.getInt(School.SELECTED_THEME, -1))
        } else {
            setDefaultTheme()
        }
    }

    private fun setDefaultTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            saveTheme(School.SYSTEM_DEFAULT)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            saveTheme(School.LIGHT_MODE)
        }
    }

    private fun saveTheme(theme: Int) {
        with(sharedPref.edit()) {
            this?.putInt(School.SELECTED_THEME, theme)
            this?.commit()
        }
    }

    private fun setSavedTheme(theme: Int) {
        when (theme) {
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
    }
}
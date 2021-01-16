package com.dan.school.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.dan.school.R
import com.dan.school.School
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsActivity : AppCompatActivity(), SettingsFragment.SettingsItemOnClickListener {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutSettings,
                    SettingsFragment.newInstance()
                ).commit()
        }
    }

    override fun itemClicked(item: Int) {
        when (item) {
            School.PROFILE -> {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(
                        R.id.frameLayoutSettings,
                        ProfileFragment(),
                        "profileFragment"
                    ).addToBackStack(null)
                    .commit()
                textViewSettingsTitle.setText(R.string.profile)
            }
            School.THEME -> {
                val items = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf("Light", "Dark", "System Default")
                } else {
                    arrayOf("Light", "Dark")
                }

                var checkedItem = when (sharedPref.getInt(School.SELECTED_THEME, -1)) {
                    School.LIGHT_MODE -> {
                        0
                    }
                    School.DARK_MODE -> {
                        1
                    }
                    School.SYSTEM_DEFAULT -> {
                        2
                    }
                    else -> {
                        0
                    }
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.theme))
                    .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
                    .setPositiveButton(resources.getString(R.string.done)) { _, _ ->
                        when (checkedItem) {
                            0 -> {
                                setTheme(School.LIGHT_MODE)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            }
                            1 -> {
                                setTheme(School.DARK_MODE)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            }
                            2 -> {
                                setTheme(School.SYSTEM_DEFAULT)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            }
                        }
                    }
                    .setSingleChoiceItems(items, checkedItem) { _, i ->
                        checkedItem = i
                    }
                    .show()
            }
            School.BACKUP -> {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(
                        R.id.frameLayoutSettings,
                        BackupFragment()
                    ).addToBackStack(null)
                    .commit()
                textViewSettingsTitle.setText(R.string.backup)
            }
            School.ABOUT -> {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(
                        R.id.frameLayoutSettings,
                        AboutFragment()
                    ).addToBackStack(null)
                    .commit()
                textViewSettingsTitle.setText(R.string.about)
            }
        }
    }
}
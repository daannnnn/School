package com.dan.school

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.dan.school.fragments.SettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_content.*

class SettingsContentFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var settingsItemOnClickListener: SettingsItemOnClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        if (parentFragment is SettingsFragment) {
            settingsItemOnClickListener = parentFragment as SettingsFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchDarkMode.isChecked = sharedPref.getBoolean(School.IS_DARK_MODE, false)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setDarkMode(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                setDarkMode(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        relativeLayoutProfile.setOnClickListener {
            if (this::settingsItemOnClickListener.isInitialized) {
                settingsItemOnClickListener.itemClicked(School.PROFILE)
            }
        }
    }

    private fun setDarkMode(isDarkMode: Boolean) {
        with(sharedPref.edit()) {
            this?.putBoolean(School.IS_DARK_MODE, isDarkMode)
            this?.commit()
        }
    }

    interface SettingsItemOnClickListener {
        fun itemClicked(item: Int)
    }
}
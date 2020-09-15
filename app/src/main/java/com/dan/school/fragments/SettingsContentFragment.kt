package com.dan.school.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.dan.school.R
import com.dan.school.School
import kotlinx.android.synthetic.main.fragment_settings_content.*

class SettingsContentFragment : Fragment() {

    private lateinit var settingsItemOnClickListener: SettingsItemOnClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
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

        if (parentFragment is SettingsFragment) {
            textViewSelectedTheme.text = (parentFragment as SettingsFragment).getSelectedTheme()
        }

        relativeLayoutProfile.setOnClickListener {
            if (this::settingsItemOnClickListener.isInitialized) {
                settingsItemOnClickListener.itemClicked(School.PROFILE)
            }
        }

        relativeLayoutTheme.setOnClickListener {
            if (this::settingsItemOnClickListener.isInitialized) {
                settingsItemOnClickListener.itemClicked(School.THEME)
            }
        }
    }

    /**
     * Updates [textViewSelectedTheme] text to [selectedTheme]
     */
    fun updateTextViewSelectedTheme(selectedTheme: String) {
        textViewSelectedTheme.text = selectedTheme
    }

    interface SettingsItemOnClickListener {
        fun itemClicked(item: Int)
    }
}
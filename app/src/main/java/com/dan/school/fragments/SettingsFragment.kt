package com.dan.school.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.dan.school.School
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val colorBackground: TypedArray = requireContext().obtainStyledAttributes(
            TypedValue().data, intArrayOf(
                android.R.attr.colorBackground
            )
        )
        requireActivity().window.statusBarColor = colorBackground.getColor(0, -1)
        colorBackground.recycle()
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun setDarkMode(isDarkMode: Boolean) {
        with(sharedPref.edit()) {
            this?.putBoolean(School.IS_DARK_MODE, isDarkMode)
            this?.commit()
        }
    }

    override fun onDestroy() {
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.appBarLayoutColor)
        super.onDestroy()
    }
}
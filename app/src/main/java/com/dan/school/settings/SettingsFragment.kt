package com.dan.school.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dan.school.*
import com.dan.school.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var settingsItemOnClickListener: SettingsItemOnClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (activity is SettingsActivity) {
            settingsItemOnClickListener = activity as SettingsActivity
        }

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

        binding.textViewSelectedTheme.text = getSelectedTheme()

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.relativeLayoutProfile.setOnClickListener {
            settingsItemOnClickListener.itemClicked(School.PROFILE)
        }

        binding.relativeLayoutTheme.setOnClickListener {
            settingsItemOnClickListener.itemClicked(School.THEME)
        }

        binding.relativeLayoutBackup.setOnClickListener {
            settingsItemOnClickListener.itemClicked(School.BACKUP)
        }

        binding.relativeLayoutAbout.setOnClickListener {
            settingsItemOnClickListener.itemClicked(School.ABOUT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.appBarLayoutColor)
        super.onDestroy()
    }

    /**
     * Saves value of [theme] to [sharedPref] with key
     * [School.SELECTED_THEME] then calls [selectedThemeUpdated]
     */
    private fun setTheme(theme: Int) {
        with(sharedPref.edit()) {
            this?.putInt(School.SELECTED_THEME, theme)
            this?.commit()
        }
        selectedThemeUpdated(theme)
    }

    /**
     * Tries to update text of [FragmentSettingsBinding.textViewSelectedTheme]
     * to the value returned by [getThemeStringWithIntValue] with [theme] as
     * parameter.
     */
    private fun selectedThemeUpdated(theme: Int) {
        binding.textViewSelectedTheme.text = getThemeStringWithIntValue(theme)
    }

    /**
     * Returns a string value that represent the given [theme]
     */
    private fun getThemeStringWithIntValue(theme: Int): String {
        return when (theme) {
            School.LIGHT_MODE -> {
                getString(R.string.light)
            }
            School.DARK_MODE -> {
                getString(R.string.dark)
            }
            School.SYSTEM_DEFAULT -> {
                getString(R.string.system_default)
            }
            else -> {
                getString(R.string.not_set)
            }
        }
    }

    private fun getSelectedTheme(): String {
        return getThemeStringWithIntValue(sharedPref.getInt(School.SELECTED_THEME, -1))
    }

    interface SettingsItemOnClickListener {
        fun itemClicked(item: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsFragment()
    }
}
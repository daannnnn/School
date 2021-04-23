package com.dan.school.ui.fragments.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dan.school.*
import com.dan.school.databinding.FragmentSettingsBinding
import com.dan.school.other.School
import com.dan.school.ui.activities.SettingsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

        binding.settingsItemTheme.setSelectedText(getSelectedTheme())

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.settingsItemProfile.setOnClickListener {
            settingsItemOnClickListener.itemClicked(School.PROFILE)
        }

        binding.settingsItemTheme.setOnClickListener {
            showSelectThemeDialog()
        }

        binding.settingsItemBackup.setOnClickListener {
            settingsItemOnClickListener.itemClicked(School.BACKUP)
        }

        binding.settingsItemAbout.setOnClickListener {
            settingsItemOnClickListener.itemClicked(School.ABOUT)
        }
    }

    private fun showSelectThemeDialog() {
        val items = getThemesArray()
        var checkedItem = getThemePosition(sharedPref.getInt(School.SELECTED_THEME, -1))
        MaterialAlertDialogBuilder(requireContext())
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

    private fun getThemePosition(theme: Int): Int {
        return when (theme) {
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
    }

    private fun getThemesArray(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                getString(R.string.light),
                getString(R.string.dark),
                getString(R.string.system_default)
            )
        } else {
            arrayOf(getString(R.string.light), getString(R.string.dark))
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
     * Sets selected text of [FragmentSettingsBinding.settingsItemTheme]
     * to the value returned by [getThemeStringWithIntValue] with [theme] as
     * parameter.
     */
    private fun selectedThemeUpdated(theme: Int) {
        binding.settingsItemTheme.setSelectedText(getThemeStringWithIntValue(theme))
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
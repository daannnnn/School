package com.dan.school.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dan.school.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment(), SettingsContentFragment.SettingsItemOnClickListener,
    MainActivity.SettingsBackPressedListener {

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
        if (activity is MainActivity) {
            (activity as MainActivity).settingsBackPressedListener = this
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .add(
                    R.id.frameLayoutSettings,
                    SettingsContentFragment()
                ).commit()
        }

        buttonBack.setOnClickListener {
            backPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onDestroy() {
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.appBarLayoutColor)
        super.onDestroy()
    }

    override fun itemClicked(item: Int) {
        when (item) {
            School.PROFILE -> {
                childFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(
                        R.id.frameLayoutSettings,
                        ProfileFragment()
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

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.title))
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
        }
    }

    override fun backPressed() {
        if (childFragmentManager.backStackEntryCount == 0) {
            if (requireActivity() is MainActivity) {
                (requireActivity() as MainActivity).supportFragmentManager.popBackStack()
            }
        } else {
            childFragmentManager.popBackStackImmediate()
            if (childFragmentManager.backStackEntryCount == 0) {
                textViewSettingsTitle.setText(R.string.settings)
            }
        }
    }

    override fun onDetach() {
        if (requireActivity() is MainActivity) {
            (requireActivity() as MainActivity).settingsBackPressedListener = null
        }
        super.onDetach()
    }

    fun setAppBarButtonRight(
        clickListener: View.OnClickListener?,
        isVisible: Boolean,
        imageRes: Int
    ) {
        buttonSettingsAppBarRight.isVisible = isVisible
        buttonSettingsAppBarRight.setImageResource(imageRes)
        buttonSettingsAppBarRight.setOnClickListener(clickListener)
    }

    fun setAppBarButtonRight(imageRes: Int) {
        buttonSettingsAppBarRight.setImageResource(imageRes)
    }

    private fun setTheme(theme: Int) {
        with(sharedPref.edit()) {
            this?.putInt(School.SELECTED_THEME, theme)
            this?.commit()
        }
    }
}
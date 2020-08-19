package com.dan.school.fragments

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dan.school.*
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment(), SettingsContentFragment.SettingsItemOnClickListener,
    MainActivity.SettingsBackPressedListener {

    override fun onAttach(context: Context) {
        super.onAttach(context)
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
                    .replace(
                        R.id.frameLayoutSettings,
                        ProfileFragment()
                    ).addToBackStack(null)
                    .commit()
                textViewSettingsTitle.setText(R.string.profile)
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
}
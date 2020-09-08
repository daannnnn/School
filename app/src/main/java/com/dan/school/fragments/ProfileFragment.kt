package com.dan.school.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.dan.school.School
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private var isEditMode = false
    private lateinit var sharedPref: SharedPreferences

    private lateinit var inputMethodManager: InputMethodManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (parentFragment is SettingsFragment) {
            (parentFragment as SettingsFragment).setAppBarButtonRight({
                (parentFragment as SettingsFragment).setAppBarButtonRight(
                    if (isEditMode) R.drawable.ic_edit
                    else R.drawable.ic_check
                )
                setEditMode(!isEditMode)
            }, true, R.drawable.ic_edit)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        constraintLayoutProfile.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        textViewNicknameDisplay.text = sharedPref.getString(School.NICKNAME, "")
        textViewFullNameDisplay.text = sharedPref.getString(School.FULL_NAME, "")
    }

    private fun setEditMode(editMode: Boolean) {
        textFieldNickname.isVisible = editMode
        textFieldFullName.isVisible = editMode
        textViewNicknameDisplay.isVisible = !editMode
        textViewFullNameDisplay.isVisible = !editMode
        if (editMode) {
            textFieldNickname.editText?.setText(sharedPref.getString(School.NICKNAME, ""))
            textFieldFullName.editText?.setText(sharedPref.getString(School.FULL_NAME, ""))
        } else {
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
            val nickname = textFieldNickname.editText?.text.toString()
            val fullName = textFieldFullName.editText?.text.toString()
            sharedPref.edit {
                putString(School.NICKNAME, nickname)
                putString(School.FULL_NAME, fullName)
                commit()
            }
            textViewNicknameDisplay.text = nickname
            textViewFullNameDisplay.text = fullName
        }
        isEditMode = editMode
    }

    override fun onDetach() {
        val currentFocusedView = requireActivity().currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
        if (parentFragment is SettingsFragment) {
            (parentFragment as SettingsFragment).setAppBarButtonRight(null, false,
                R.drawable.ic_edit
            )
        }
        super.onDetach()
    }
}
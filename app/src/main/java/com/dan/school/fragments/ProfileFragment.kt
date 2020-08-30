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
import androidx.fragment.app.activityViewModels
import com.dan.school.DataViewModel
import com.dan.school.R
import com.dan.school.School
import com.dan.school.models.Profile
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private var isEditMode = false

    private lateinit var inputMethodManager: InputMethodManager
    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (parentFragment is SettingsFragment) {
            (parentFragment as SettingsFragment).setAppBarButtonRight(View.OnClickListener {
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

        textViewNicknameDisplay.text = dataViewModel.profile.value!!.nickname
        textViewFullNameDisplay.text = dataViewModel.profile.value!!.fullName
    }

    private fun setEditMode(editMode: Boolean) {
        textFieldNickname.isVisible = editMode
        textFieldFullName.isVisible = editMode
        textViewNicknameDisplay.isVisible = !editMode
        textViewFullNameDisplay.isVisible = !editMode
        if (editMode) {
            textFieldNickname.editText?.setText(dataViewModel.profile.value!!.nickname)
            textFieldFullName.editText?.setText(dataViewModel.profile.value!!.fullName)
        } else {
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
            val nickname = textFieldNickname.editText?.text.toString()
            val fullName = textFieldFullName.editText?.text.toString()
            dataViewModel.updateProfile(Profile(fullName, nickname))
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
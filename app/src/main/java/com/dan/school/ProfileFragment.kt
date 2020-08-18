package com.dan.school

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.dan.school.fragments.SettingsFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private var isEditMode = false
    private lateinit var sharedPref: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
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

        textViewNickNameDisplay.text = sharedPref.getString(School.NICKNAME, "")
        textViewFullNameDisplay.text = sharedPref.getString(School.FULL_NAME, "")
        textViewEmailDisplay.text = sharedPref.getString(School.EMAIL, "")
    }

    private fun setEditMode(editMode: Boolean) {
        textFieldNickname.isVisible = editMode
        textFieldFullName.isVisible = editMode
        textFieldEmail.isVisible = editMode
        textViewNickNameDisplay.isVisible = !editMode
        textViewFullNameDisplay.isVisible = !editMode
        textViewEmailDisplay.isVisible = !editMode
        if (editMode) {
            textFieldNickname.editText?.setText(sharedPref.getString(School.NICKNAME, ""))
            textFieldFullName.editText?.setText(sharedPref.getString(School.FULL_NAME, ""))
            textFieldEmail.editText?.setText(sharedPref.getString(School.EMAIL, ""))
        } else {
            val nickname = textFieldNickname.editText?.text.toString()
            val fullName = textFieldFullName.editText?.text.toString()
            val email = textFieldEmail.editText?.text.toString()
            sharedPref.edit {
                putString(School.NICKNAME, nickname)
                putString(School.FULL_NAME, fullName)
                putString(School.EMAIL, email)
                commit()
            }
            textViewNickNameDisplay.text = nickname
            textViewFullNameDisplay.text = fullName
            textViewEmailDisplay.text = email
        }
        isEditMode = editMode
    }
}
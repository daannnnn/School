package com.dan.school.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import com.dan.school.School
import kotlinx.android.synthetic.main.fragment_profile_setup.*

class ProfileSetupFragment : Fragment() {

    private var nickname = ""
    private var fullName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nickname = it.getString(School.NICKNAME, "")
            fullName = it.getString(School.FULL_NAME, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textFieldNickname.editText?.setText(nickname)
        textFieldFullName.editText?.setText(fullName)

        buttonDone.setOnClickListener {
            val isNicknameEmpty = textFieldNickname.editText?.text.toString().trim().isEmpty()
            val isFullNameEmpty = textFieldFullName.editText?.text.toString().trim().isEmpty()
            if (isNicknameEmpty) {
                textFieldNickname.error = getString(R.string.this_field_is_required)
            } else {
                textFieldNickname.error = null
            }
            if (isFullNameEmpty) {
                textFieldFullName.error = getString(R.string.this_field_is_required)
            } else {
                textFieldFullName.error = null
            }
            if (!(isNicknameEmpty || isFullNameEmpty)) {
                if (activity is SetupActivity) {
                    (activity as SetupActivity).profileSetupDone(
                        textFieldNickname.editText?.text.toString().trim(),
                        textFieldFullName.editText?.text.toString().trim()
                    )
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(nickname: String = "", fullName: String = "") =
            ProfileSetupFragment().apply {
                arguments = Bundle().apply {
                    putString(School.NICKNAME, nickname)
                    putString(School.FULL_NAME, fullName)
                }
            }
    }

}
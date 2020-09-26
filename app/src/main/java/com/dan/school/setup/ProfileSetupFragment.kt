package com.dan.school.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import com.dan.school.SetupActivity
import kotlinx.android.synthetic.main.fragment_profile_setup.*

class ProfileSetupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonDone.setOnClickListener {
            val isNicknameEmpty = textFieldNickname.editText?.text.toString().trim().isEmpty()
            val isFullNameEmpty = textFieldFullName.editText?.text.toString().trim().isEmpty()
            if (isNicknameEmpty) {
                textFieldNickname.error = "This field is required."
            } else {
                textFieldNickname.error = null
            }
            if (isFullNameEmpty) {
                textFieldFullName.error = "This field is required."
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

}
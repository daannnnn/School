package com.dan.school.setup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import com.dan.school.School
import com.dan.school.databinding.FragmentProfileSetupBinding

class ProfileSetupFragment : Fragment() {

    private var _binding: FragmentProfileSetupBinding? = null

    private val binding get() = _binding!!

    private lateinit var profileSetupDoneListener: ProfileSetupDoneListener

    private var nickname = ""
    private var fullName = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is SetupActivity) {
            profileSetupDoneListener = activity as SetupActivity
        }
    }

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
    ): View {
        _binding = FragmentProfileSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textFieldNickname.editText?.setText(nickname)
        binding.textFieldFullName.editText?.setText(fullName)

        binding.buttonDone.setOnClickListener {
            if (isInputValid()) {
                saveProfile()
            }
        }
    }

    private fun saveProfile() {
        if (this::profileSetupDoneListener.isInitialized) {
            profileSetupDoneListener.profileSetupDone(
                binding.textFieldNickname.editText?.text.toString().trim(),
                binding.textFieldFullName.editText?.text.toString().trim()
            )
        }
    }

    private fun isInputValid(): Boolean {
        val isNicknameEmpty = binding.textFieldNickname.editText?.text.toString().trim().isEmpty()
        val isFullNameEmpty = binding.textFieldFullName.editText?.text.toString().trim().isEmpty()
        if (isNicknameEmpty) {
            binding.textFieldNickname.error = getString(R.string.this_field_is_required)
        } else {
            binding.textFieldNickname.error = null
        }
        if (isFullNameEmpty) {
            binding.textFieldFullName.error = getString(R.string.this_field_is_required)
        } else {
            binding.textFieldFullName.error = null
        }
        return !(isNicknameEmpty || isFullNameEmpty)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface ProfileSetupDoneListener {
        fun profileSetupDone(nickname: String, fullName: String)
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
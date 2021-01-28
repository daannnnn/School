package com.dan.school.authentication

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.dan.school.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null

    private val binding get() = _binding!!

    private lateinit var signUpButtonClickListener: SignUpButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AuthenticationActivity) {
            signUpButtonClickListener = activity as AuthenticationActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTextEmail.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (binding.editTextEmail.editText!!.text.trim().isNotEmpty()) {
                    binding.editTextEmail.error = ""
                } else {
                    binding.editTextEmail.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        binding.editTextPassword.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (binding.editTextPassword.editText!!.text.trim().isNotEmpty()) {
                    binding.editTextPassword.error = ""
                } else {
                    binding.editTextPassword.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        binding.editTextFullName.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (binding.editTextFullName.editText!!.text.trim().isNotEmpty()) {
                    binding.editTextFullName.error = ""
                } else {
                    binding.editTextFullName.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        binding.editTextNickname.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (binding.editTextNickname.editText!!.text.trim().isNotEmpty()) {
                    binding.editTextNickname.error = ""
                } else {
                    binding.editTextNickname.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        binding.buttonSignUp.setOnClickListener {
            var cancel = false

            if (binding.editTextEmail.editText!!.text.trim().isNotEmpty()) {
                binding.editTextEmail.error = ""
            } else {
                binding.editTextEmail.error = getString(R.string.this_field_is_required)
                cancel = true
            }
            if (binding.editTextPassword.editText!!.text.trim().isNotEmpty()) {
                binding.editTextPassword.error = ""
            } else {
                binding.editTextPassword.error = getString(R.string.this_field_is_required)
                cancel = true
            }
            if (binding.editTextFullName.editText!!.text.trim().isNotEmpty()) {
                binding.editTextFullName.error = ""
            } else {
                binding.editTextFullName.error = getString(R.string.this_field_is_required)
                cancel = true
            }
            if (binding.editTextNickname.editText!!.text.trim().isNotEmpty()) {
                binding.editTextNickname.error = ""
            } else {
                binding.editTextNickname.error = getString(R.string.this_field_is_required)
                cancel = true
            }

            if (!cancel) {
                signUpButtonClickListener.signUpButtonClicked(
                    binding.editTextEmail.editText!!.text.toString(),
                    binding.editTextPassword.editText!!.text.toString(),
                    binding.editTextNickname.editText!!.text.toString(),
                    binding.editTextFullName.editText!!.text.toString()
                )
            }
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface SignUpButtonClickListener {
        fun signUpButtonClicked(email: String, password: String, nickname: String, fullName: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SignUpFragment()
    }
}
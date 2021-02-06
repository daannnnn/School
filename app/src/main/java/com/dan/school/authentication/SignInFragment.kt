package com.dan.school.authentication

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import com.dan.school.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null

    private val binding get() = _binding!!

    private lateinit var signInButtonClickListener: SignInButtonClickListener
    private lateinit var forgotPasswordButtonClickListener: ForgotPasswordButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AuthenticationActivity) {
            signInButtonClickListener = activity as AuthenticationActivity
            forgotPasswordButtonClickListener = activity as AuthenticationActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
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

        binding.buttonSignIn.setOnClickListener {
            var isInputValid = true

            if (binding.editTextEmail.editText!!.text.trim().isNotEmpty()) {
                binding.editTextEmail.error = ""
            } else {
                binding.editTextEmail.error = getString(R.string.this_field_is_required)
                isInputValid = false
            }
            if (binding.editTextPassword.editText!!.text.trim().isNotEmpty()) {
                binding.editTextPassword.error = ""
            } else {
                binding.editTextPassword.error = getString(R.string.this_field_is_required)
                isInputValid = false
            }

            if (isInputValid) {
                signInButtonClickListener.signInButtonClicked(
                    binding.editTextEmail.editText!!.text.toString(),
                    binding.editTextPassword.editText!!.text.toString()
                )
            }
        }
        binding.buttonForgotPassword.setOnClickListener {
            forgotPasswordButtonClickListener.forgotPasswordButtonClicked()
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface SignInButtonClickListener {
        fun signInButtonClicked(email: String, password: String)
    }

    interface ForgotPasswordButtonClickListener {
        fun forgotPasswordButtonClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SignInFragment()
    }
}
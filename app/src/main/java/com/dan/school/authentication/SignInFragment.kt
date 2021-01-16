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
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SignInFragment : Fragment() {

    private lateinit var signInButtonClickListener: SignInButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AuthenticationActivity) {
            signInButtonClickListener = activity as AuthenticationActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextEmail.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (editTextEmail.editText!!.text.trim().isNotEmpty()) {
                    editTextEmail.error = ""
                } else {
                    editTextEmail.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        editTextPassword.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (editTextPassword.editText!!.text.trim().isNotEmpty()) {
                    editTextPassword.error = ""
                } else {
                    editTextPassword.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        buttonSignIn.setOnClickListener {
            var cancel = false

            if (editTextEmail.editText!!.text.trim().isNotEmpty()) {
                editTextEmail.error = ""
            } else {
                editTextEmail.error = getString(R.string.this_field_is_required)
                cancel = true
            }
            if (editTextPassword.editText!!.text.trim().isNotEmpty()) {
                editTextPassword.error = ""
            } else {
                editTextPassword.error = getString(R.string.this_field_is_required)
                cancel = true
            }

            if (!cancel) {
                signInButtonClickListener.signInButtonClicked(
                    editTextEmail.editText!!.text.toString(),
                    editTextPassword.editText!!.text.toString()
                )
            }
        }
        buttonForgotPassword.setOnClickListener {
            (activity as AuthenticationActivity).goToFP()
        }

        buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    interface SignInButtonClickListener {
        fun signInButtonClicked(email: String, password: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SignInFragment()
    }
}
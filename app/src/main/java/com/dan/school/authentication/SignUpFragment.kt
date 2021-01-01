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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignUpFragment : Fragment() {

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
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
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
        editTextFullName.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (editTextFullName.editText!!.text.trim().isNotEmpty()) {
                    editTextFullName.error = ""
                } else {
                    editTextFullName.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        editTextNickname.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (editTextNickname.editText!!.text.trim().isNotEmpty()) {
                    editTextNickname.error = ""
                } else {
                    editTextNickname.error = getString(R.string.this_field_is_required)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        buttonSignUp.setOnClickListener {
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
            if (editTextFullName.editText!!.text.trim().isNotEmpty()) {
                editTextFullName.error = ""
            } else {
                editTextFullName.error = getString(R.string.this_field_is_required)
                cancel = true
            }
            if (editTextNickname.editText!!.text.trim().isNotEmpty()) {
                editTextNickname.error = ""
            } else {
                editTextNickname.error = getString(R.string.this_field_is_required)
                cancel = true
            }

            if (!cancel) {
                signUpButtonClickListener.signUpButtonClicked(
                    editTextEmail.editText!!.text.toString(),
                    editTextPassword.editText!!.text.toString()
                )
            }
        }
    }

    interface SignUpButtonClickListener {
        fun signUpButtonClicked(email: String, password: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SignUpFragment()
    }
}
package com.dan.school.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dan.school.R
import com.dan.school.School
import kotlinx.android.synthetic.main.fragment_authentication.*

class AuthenticationFragment : Fragment() {

    private lateinit var buttonSignInWithClickListener: ButtonSignInWithClickListener
    private lateinit var buttonSignUpClickListener: ButtonSignUpClickListener
    private lateinit var buttonSignInLaterClickListener: ButtonSignInLaterClickListener

    private var showButtonSignInLater = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AuthenticationActivity) {
            buttonSignInWithClickListener = activity as AuthenticationActivity
            buttonSignUpClickListener = activity as AuthenticationActivity
            buttonSignInLaterClickListener = activity as AuthenticationActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            showButtonSignInLater = it.getBoolean(School.FROM_SETUP)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_authentication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonSignInLater.isVisible = showButtonSignInLater

        buttonSignInWithEmail.setOnClickListener {
            buttonSignInWithClickListener.buttonSignInWithClicked(School.SIGN_IN_WITH_EMAIL)
        }
        buttonSignInWithGoogle.setOnClickListener {
            buttonSignInWithClickListener.buttonSignInWithClicked(School.SIGN_IN_WITH_GOOGLE)
        }
        buttonSignInLater.setOnClickListener {
            buttonSignInLaterClickListener.buttonSignInLaterClicked()
        }
        buttonSignUp.setOnClickListener {
            buttonSignUpClickListener.buttonSignUpClicked()
        }

    }

    interface ButtonSignInWithClickListener {
        fun buttonSignInWithClicked(signInWith: Int)
    }

    interface ButtonSignUpClickListener {
        fun buttonSignUpClicked()
    }

    interface ButtonSignInLaterClickListener {
        fun buttonSignInLaterClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance(fromSetup: Boolean) =
            AuthenticationFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(School.FROM_SETUP, fromSetup)
                }
            }
    }

}
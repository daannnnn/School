package com.dan.school.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.dan.school.School
import com.dan.school.databinding.FragmentAuthenticationBinding

class AuthenticationFragment : Fragment() {

    private var _binding: FragmentAuthenticationBinding? = null

    private val binding get() = _binding!!

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
            showButtonSignInLater = it.getBoolean(School.SHOW_BUTTON_SIGN_IN_LATER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignInLater.isVisible = showButtonSignInLater

        binding.buttonSignInWithEmail.setOnClickListener {
            buttonSignInWithClickListener.buttonSignInWithClicked(School.SIGN_IN_WITH_EMAIL)
        }
        binding.buttonSignInWithGoogle.setOnClickListener {
            buttonSignInWithClickListener.buttonSignInWithClicked(School.SIGN_IN_WITH_GOOGLE)
        }
        binding.buttonSignInLater.setOnClickListener {
            buttonSignInLaterClickListener.buttonSignInLaterClicked()
        }
        binding.buttonSignUp.setOnClickListener {
            buttonSignUpClickListener.buttonSignUpClicked()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        fun newInstance(showButtonSignInLater: Boolean) =
            AuthenticationFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(School.SHOW_BUTTON_SIGN_IN_LATER, showButtonSignInLater)
                }
            }
    }

}
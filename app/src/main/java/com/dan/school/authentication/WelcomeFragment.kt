package com.dan.school.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import kotlinx.android.synthetic.main.fragment_welcome.*

private const val EMAIL_VERIFICATION_SENT = "email_verification_sent"
private const val EMAIL = "email"

class WelcomeFragment : Fragment() {

    private lateinit var welcomeDoneButtonClickListener: WelcomeDoneButtonClickListener

    private var emailVerificationSent: Boolean = false
    private var email: String = "your email"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            emailVerificationSent = it.getBoolean(EMAIL_VERIFICATION_SENT)
            val s = it.getString(EMAIL)
            if (s != null) {
                email = s
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AuthenticationActivity) {
            welcomeDoneButtonClickListener = activity as AuthenticationActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (emailVerificationSent) {
            cardViewVerifyEmail.visibility = View.VISIBLE
            val message =
                "${getString(R.string.a_verification_email_has_been_sent_to)} $email. ${getString(R.string.please_check_your_email_to_verify_your_account)}"
            textViewVerifyEmailMessage.text = message
        }
        buttonDone.setOnClickListener {
            welcomeDoneButtonClickListener.welcomeDoneButtonClicked()
        }
    }

    interface WelcomeDoneButtonClickListener {
        fun welcomeDoneButtonClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance(emailVerificationSent: Boolean, email: String) =
            WelcomeFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(EMAIL_VERIFICATION_SENT, emailVerificationSent)
                    putString(EMAIL, email)
                }
            }
    }
}
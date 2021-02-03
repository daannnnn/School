package com.dan.school.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import com.dan.school.School
import com.dan.school.databinding.FragmentWelcomeBinding

private const val EMAIL_VERIFICATION_SENT = "email_verification_sent"
private const val EMAIL = "email"

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var welcomeDoneButtonClickListener: WelcomeDoneButtonClickListener

    private var emailVerificationSent: Boolean = false
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = getString(R.string.your_email)
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
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (emailVerificationSent) {
            binding.cardViewVerifyEmail.visibility = View.VISIBLE
            val message =
                "${getString(R.string.a_verification_email_has_been_sent_to)} $email. ${getString(R.string.please_check_your_email_to_verify_your_account)}"
            binding.textViewVerifyEmailMessage.text = message
        }

        val sharedPref = requireContext().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val nickname = sharedPref.getString(School.NICKNAME, "")
        val welcomeMessage =
            "${getString(R.string.welcome)}${if (nickname != "") " $nickname" else ""}!"
        binding.textViewWelcome.text = welcomeMessage
        binding.buttonDone.setOnClickListener {
            welcomeDoneButtonClickListener.welcomeDoneButtonClicked()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
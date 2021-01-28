package com.dan.school.authentication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.dan.school.School
import com.dan.school.databinding.FragmentResetPasswordBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.math.ceil


class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private var allowSendResetPasswordEmail = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
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
        binding.buttonResetPassword.setOnClickListener {
            sendPasswordResetEmail()
        }
        binding.buttonResendResetPassword.setOnClickListener {
            sendPasswordResetEmail()
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Sends a password reset email to email from [editTextEmail] if the
     * difference between [School.PASSWORD_RESET_EMAIL_TIME_LAST_SENT]
     * saved in [sharedPref] and the [System.currentTimeMillis] in seconds
     * is greater than or equal to 30.
     */
    private fun sendPasswordResetEmail() {
        val lastPasswordResetTime = sharedPref.getLong(
            School.PASSWORD_RESET_EMAIL_TIME_LAST_SENT,
            0
        )
        val time = (System.currentTimeMillis() - lastPasswordResetTime).toFloat() / 1000

        if (time >= 30) {
            val email = binding.editTextEmail.editText!!.text.toString()
            if (email.trim().isNotEmpty() && allowSendResetPasswordEmail) {
                binding.editTextEmail.error = ""
                Firebase.auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.constraintLayoutResetPassword.visibility = View.GONE
                            binding.cardViewResetPasswordMessage.visibility = View.VISIBLE

                            val message =
                                "${getString(R.string.a_password_reset_email_has_been_sent_to)} $email."
                            binding.textViewResetPasswordMessage.text = message

                            with(sharedPref.edit()) {
                                putLong(
                                    School.PASSWORD_RESET_EMAIL_TIME_LAST_SENT,
                                    System.currentTimeMillis()
                                )
                                apply()
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.an_error_occurred),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        allowSendResetPasswordEmail = true
                    }
            } else {
                binding.editTextEmail.error = getString(R.string.this_field_is_required)
            }
        } else {
            val timeToWait = ceil(30 - time).toInt()
            Toast.makeText(
                requireContext(),
                "${getString(R.string.please_try_again_in)} $timeToWait ${
                    if (timeToWait == 1) getString(
                        R.string.second
                    ) else getString(R.string.seconds)
                }.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ResetPasswordFragment()
    }
}
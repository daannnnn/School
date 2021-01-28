package com.dan.school.settings

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dan.school.ProgressBarDialog
import com.dan.school.R
import com.dan.school.School
import com.dan.school.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.ceil

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    private var isSendingVerificationEmail = false
    private var isEditMode = false
    private lateinit var sharedPref: SharedPreferences

    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var progressBarDialog: ProgressBarDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressBarDialog = ProgressBarDialog(requireContext())

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEditMode) {
                        MaterialAlertDialogBuilder(requireContext()).setMessage(null)
                            .setTitle(getString(R.string.do_you_want_to_save_your_changes))
                            .setPositiveButton(getString(R.string.save)) { _, _ ->
                                setEditMode(false)
                                // save to realtime database save updated time
                                requireActivity().supportFragmentManager.popBackStack()
                            }
                            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                            .create()
                            .show()
                    } else {
                        if (requireActivity() is SettingsActivity) {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        auth = Firebase.auth
        database = Firebase.database
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.swipeRefreshLayout.isRefreshing = true
        update()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.constraintLayoutProfile.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        binding.textViewNicknameDisplay.text = sharedPref.getString(School.NICKNAME, "")
        binding.textViewFullNameDisplay.text = sharedPref.getString(School.FULL_NAME, "")

        binding.buttonSendVerificationEmail.setOnClickListener {
            sendVerificationEmail()
        }

        binding.buttonEdit.setOnClickListener {
            if (isEditMode) {
                binding.buttonEdit.setImageResource(R.drawable.ic_edit)
                setEditMode(false)
            } else {
                binding.buttonEdit.setImageResource(R.drawable.ic_check)
                setEditMode(true)
            }
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.buttonResetPassword.setOnClickListener {

            val lastPasswordResetTime = sharedPref.getLong(
                School.PASSWORD_RESET_EMAIL_TIME_LAST_SENT,
                0
            )
            val time = (System.currentTimeMillis() - lastPasswordResetTime).toFloat() / 1000

            if (time >= 30) {
                MaterialAlertDialogBuilder(requireContext()).setMessage("${getString(R.string.send_password_reset_email_to)} ${auth.currentUser?.email}.")
                    .setTitle("${getString(R.string.reset_password)}?")
                    .setPositiveButton(
                        getString(R.string.yes)
                    ) { _, _ ->
                        showProgressBar()
                        auth.currentUser?.email?.let { email ->
                            auth.sendPasswordResetEmail(email).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.password_reset_email_sent),
                                        Toast.LENGTH_SHORT
                                    ).show()

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
                                hideProgressBar()
                            }
                        }
                    }
                    .setNegativeButton(
                        getString(R.string.no)
                    ) { _, _ -> }
                    .create()
                    .show()
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            update()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showProgressBar() {
        progressBarDialog.show()
    }

    private fun hideProgressBar() {
        progressBarDialog.dismiss()
    }

    /**
     * Updates the visibility of [FragmentProfileBinding.groupEmail],
     * [FragmentProfileBinding.viewDivider],
     * [FragmentProfileBinding.buttonResetPassword] and
     * [FragmentProfileBinding.cardViewVerifyEmail] depending if a
     * user is signed-in and if the email is verified.
     * Sets the text of [FragmentProfileBinding.textViewEmailDisplay]
     * to the current user's email.
     */
    private fun update() {
        val user = auth.currentUser
        if (user != null) {

            binding.groupEmail.visibility = View.VISIBLE
            binding.viewDivider.visibility = View.VISIBLE
            binding.buttonResetPassword.visibility = View.VISIBLE

            binding.textViewEmailDisplay.text = user.email
            binding.cardViewVerifyEmail.isGone = user.isEmailVerified

            user.reload().addOnCompleteListener {
                if (it.isSuccessful) {
                    binding.textViewEmailDisplay.text = user.email
                    binding.cardViewVerifyEmail.isGone = user.isEmailVerified
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_update), Toast.LENGTH_SHORT).show()
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }
        } else {
            binding.groupEmail.visibility = View.GONE
            binding.viewDivider.visibility = View.GONE
            binding.buttonResetPassword.visibility = View.GONE
            binding.cardViewVerifyEmail.isGone = true
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    /**
     * Sends email verification on currently signed-in user if
     * the last time a verification email was sent is 30 seconds
     * before [System.currentTimeMillis].
     */
    private fun sendVerificationEmail() {
        val time = (System.currentTimeMillis() - sharedPref.getLong(
            School.VERIFICATION_EMAIL_TIME_LAST_SENT,
            0
        )).toFloat() / 1000
        if (time >= 30) {
            if (!isSendingVerificationEmail) {
                isSendingVerificationEmail = true
                binding.cardViewVerifyEmail.visibility = View.VISIBLE
                auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        with(sharedPref.edit()) {
                            putLong(
                                School.VERIFICATION_EMAIL_TIME_LAST_SENT,
                                System.currentTimeMillis()
                            )
                            apply()
                        }
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.verification_email_sent),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_to_send_verification_email),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isSendingVerificationEmail = false
                }
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

    private fun setEditMode(editMode: Boolean) {
        binding.textFieldNickname.isVisible = editMode
        binding.textFieldFullName.isVisible = editMode
        binding.textViewNicknameDisplay.isVisible = !editMode
        binding.textViewFullNameDisplay.isVisible = !editMode
        if (editMode) {
            binding.textFieldNickname.editText?.setText(sharedPref.getString(School.NICKNAME, ""))
            binding.textFieldFullName.editText?.setText(sharedPref.getString(School.FULL_NAME, ""))
        } else {
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
            val nickname = binding.textFieldNickname.editText?.text.toString()
            val fullName = binding.textFieldFullName.editText?.text.toString()
            sharedPref.edit {
                putString(School.NICKNAME, nickname)
                putString(School.FULL_NAME, fullName)
                commit()
            }
            binding.textViewNicknameDisplay.text = nickname
            binding.textViewFullNameDisplay.text = fullName

            sharedPref.edit().putBoolean(School.DATABASE_PROFILE_UPDATED, false).apply()

            auth.currentUser?.let {
                val map = mapOf(
                    School.NICKNAME to nickname,
                    School.FULL_NAME to fullName,
                    School.PROFILE_LAST_UPDATE_TIME to ServerValue.TIMESTAMP
                )
                database.reference.child(School.USERS).child(it.uid).updateChildren(map)
                    .addOnSuccessListener {
                        sharedPref.edit().putBoolean(School.DATABASE_PROFILE_UPDATED, true)
                    }
            }
        }
        isEditMode = editMode
    }

    override fun onDetach() {
        val currentFocusedView = requireActivity().currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
        super.onDetach()
    }
}
package com.dan.school.ui.fragments.settings

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.dan.school.other.School
import com.dan.school.databinding.FragmentProfileBinding
import com.dan.school.ui.activities.SettingsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    private var isEditMode = false
    private lateinit var sharedPref: SharedPreferences

    private lateinit var inputMethodManager: InputMethodManager

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

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEditMode) {
                        showSaveChangesDialog()
                    } else {
                        if (requireActivity() is SettingsActivity) {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun showSaveChangesDialog() {
        MaterialAlertDialogBuilder(requireContext()).setMessage(null)
            .setTitle(getString(R.string.do_you_want_to_save_your_changes))
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                if (setEditMode(false)) {
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.enter_a_valid_input),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.constraintLayoutProfile.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        binding.textViewNicknameDisplay.text = sharedPref.getString(School.NICKNAME, "")
        binding.textViewFullNameDisplay.text = sharedPref.getString(School.FULL_NAME, "")

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setEditMode(editMode: Boolean): Boolean {
        if (editMode) {
            binding.textFieldNickname.editText?.setText(sharedPref.getString(School.NICKNAME, ""))
            binding.textFieldFullName.editText?.setText(sharedPref.getString(School.FULL_NAME, ""))
            isEditMode = editMode
            setVisibility(editMode)
            return true
        } else {
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
            val nickname = binding.textFieldNickname.editText?.text.toString()
            val fullName = binding.textFieldFullName.editText?.text.toString()

            val isNicknameEmpty = nickname.trim().isEmpty()
            val isFullNameEmpty = fullName.trim().isEmpty()
            if (isNicknameEmpty) {
                binding.textFieldNickname.error = getString(R.string.this_field_is_required)
            } else {
                binding.textFieldNickname.error = null
            }
            if (isFullNameEmpty) {
                binding.textFieldFullName.error = getString(R.string.this_field_is_required)
            } else {
                binding.textFieldFullName.error = null
            }
            if (!(isNicknameEmpty || isFullNameEmpty)) {
                binding.textViewNicknameDisplay.text = nickname
                binding.textViewFullNameDisplay.text = fullName

                saveUpdatedProfile(nickname, fullName)
                isEditMode = editMode
                setVisibility(editMode)
                return true
            }
        }
        return false
    }

    private fun saveUpdatedProfile(nickname: String, fullName: String) {
        sharedPref.edit {
            putString(School.NICKNAME, nickname)
            putString(School.FULL_NAME, fullName)
            commit()
        }
    }

    private fun setVisibility(editMode: Boolean) {
        binding.textFieldNickname.isVisible = editMode
        binding.textFieldFullName.isVisible = editMode
        binding.textViewNicknameDisplay.isVisible = !editMode
        binding.textViewFullNameDisplay.isVisible = !editMode
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
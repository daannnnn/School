package com.dan.school.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.dan.school.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmDeleteDialogFragment(
    private val confirmDeleteListener: ConfirmDeleteListener,
    private val itemId: Int,
    private val title: String
) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialog =
                MaterialAlertDialogBuilder(it).setMessage(R.string.are_you_sure_you_want_to_delete_this_item)
                    .setTitle(title)
                    .setPositiveButton(
                        R.string.yes
                    ) { _, _ ->
                        confirmDeleteListener.confirmDelete(itemId)
                    }
                    .setNegativeButton(
                        R.string.cancel
                    ) { _, _ -> }
                    .create()
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface ConfirmDeleteListener {
        fun confirmDelete(itemId: Int)
    }
}
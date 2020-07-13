package com.dan.school

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(private val listener: DatePickerDialog.OnDateSetListener, private val onCancelListener: OnCancelListener?) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(), listener, year, month, day)
    }

    override fun onCancel(dialog: DialogInterface) {
        onCancelListener?.canceled()
        super.onCancel(dialog)
    }

    interface OnCancelListener {
        fun canceled()
    }
}
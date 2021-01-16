package com.dan.school

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BackupItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textViewBackupFileName: TextView = view.findViewById(R.id.textViewBackupFileName)
    val textViewBackupDate: TextView = view.findViewById(R.id.textViewBackupDate)
}

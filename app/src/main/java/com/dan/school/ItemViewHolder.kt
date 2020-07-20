package com.dan.school

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textViewItem: TextView = view.findViewById(R.id.textViewItem)
    val buttonCheckItem: ImageButton = view.findViewById(R.id.buttonCheck)
    val buttonSubtask: ImageButton = view.findViewById(R.id.buttonSubtask)
}
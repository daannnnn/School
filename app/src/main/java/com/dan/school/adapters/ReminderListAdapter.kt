package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.models.Reminder

class ReminderListAdapter(
    private val context: Context,
    val data: ArrayList<Reminder>
): RecyclerView.Adapter<ReminderListAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val textViewReminderDate: TextView = view.findViewById(R.id.textViewReminderDate)
        val textViewReminderTime: TextView = view.findViewById(R.id.textViewReminderTime)
        val buttonRemoveReminder: ImageButton = view.findViewById(R.id.buttonRemoveReminder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_reminder_item, parent, false)
        return ReminderViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.textViewReminderDate.text = data[position].getDateString()
        holder.textViewReminderTime.text = data[position].getTimeString()
        holder.buttonRemoveReminder.setOnClickListener {
            data.removeAt(holder.bindingAdapterPosition)
            notifyItemRemoved(holder.bindingAdapterPosition)
        }
    }
}
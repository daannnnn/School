package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.models.Subtask

class SubtasksShowListAdapter(
    private val context: Context,
    private val subtasks: ArrayList<Subtask>
) :
    RecyclerView.Adapter<SubtasksShowListAdapter.SubtasksShowViewHolder>() {

    class SubtasksShowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewSubtaskTitle: TextView = view.findViewById(R.id.textViewSubtaskTitle)
        val buttonCheck: ImageButton = view.findViewById(R.id.buttonCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtasksShowViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_subtask_show_item, parent, false)
        return SubtasksShowViewHolder(view)
    }

    override fun getItemCount(): Int {
        return subtasks.size
    }

    override fun onBindViewHolder(holder: SubtasksShowViewHolder, position: Int) {
        holder.textViewSubtaskTitle.text = subtasks[position].title
        if (subtasks[position].done) {
            holder.buttonCheck.setImageResource(R.drawable.ic_homework_checked)
        } else {
            holder.buttonCheck.setImageResource(R.drawable.ic_homework_unchecked)
        }
    }
}
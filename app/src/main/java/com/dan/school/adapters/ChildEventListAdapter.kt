package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.ItemViewHolder
import com.dan.school.R
import com.dan.school.models.Event
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import java.util.*

class ChildEventListAdapter(
    private val doneListener: DoneListener,
    private val showSubtasksListener: ShowSubtasksListener,
    private val calendarItemClickListener: CalendarItemClickListener,
    private val events: ArrayList<Event>,
    private val context: Context
) :
    RecyclerView.Adapter<ItemViewHolder>() {

    private val categoryCheckedIcons = arrayOf(
        R.drawable.ic_homework_checked,
        R.drawable.ic_exam_checked,
        R.drawable.ic_task_checked
    )
    private val categoryUncheckedIcons = arrayOf(
        R.drawable.ic_homework_unchecked,
        R.drawable.ic_exam_unchecked,
        R.drawable.ic_task_unchecked
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (events[position].done) {
            holder.buttonCheckItem.setImageResource(categoryCheckedIcons[events[position].category])
        } else {
            holder.buttonCheckItem.setImageResource(categoryUncheckedIcons[events[position].category])
        }
        holder.buttonSubtask.isVisible = events[position].subtasks.isNotEmpty()

        holder.buttonSubtask.setOnClickListener {
            showSubtasksListener.showSubtasks(
                events[holder.bindingAdapterPosition].subtasks,
                events[holder.bindingAdapterPosition].title,
                events[holder.bindingAdapterPosition].id,
                events[holder.bindingAdapterPosition].category
            )
        }
        holder.itemView.setOnClickListener {
            calendarItemClickListener.calendarItemClicked(events[holder.bindingAdapterPosition].id)
        }
        holder.textViewItem.text = events[position].title
        holder.buttonCheckItem.setOnClickListener {
            doneListener.setDone(events[holder.bindingAdapterPosition].id, !events[position].done)
        }
    }

    interface DoneListener {
        fun setDone(id: Int, done: Boolean)
    }

    interface ShowSubtasksListener {
        fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int, category: Int)
    }

    interface CalendarItemClickListener {
        fun calendarItemClicked(id: Int)
    }
}
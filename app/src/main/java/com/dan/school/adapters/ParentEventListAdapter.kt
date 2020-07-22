package com.dan.school.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.School
import com.dan.school.fragments.CalendarFragment
import com.dan.school.models.CategoryEventList
import com.dan.school.models.Item
import com.dan.school.models.Subtask

class ParentEventListAdapter(
    var events: ArrayList<CategoryEventList>,
    private val context: Context,
    private val doneListener: DoneListener,
    private val showSubtasksListener: ShowSubtasksListener,
    private val calendarItemClickListener: CalendarItemClickListener
) :
    RecyclerView.Adapter<ParentEventListAdapter.EventViewHolder>(),
    ChildEventListAdapter.DoneListener, ChildEventListAdapter.ShowSubtasksListener,
    ChildEventListAdapter.CalendarItemClickListener {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerViewEventsChild: RecyclerView = view.findViewById(R.id.recyclerViewEventsChild)
        val textViewCategory: TextView = view.findViewById(R.id.textViewCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_events_parent_recycler_view, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val childEventListAdapter = ChildEventListAdapter(this, this, this, events[position].events, context)
        holder.recyclerViewEventsChild.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = childEventListAdapter
        }
        holder.textViewCategory.text =
            when (events[position].category) {
                School.HOMEWORK -> {
                    "Homework"
                }
                School.EXAM -> {
                    "Exam"
                }
                else -> {
                    "Task"
                }
            }
    }

    interface DoneListener {
        fun setDone(id: Int, done: Boolean)
    }

    override fun setDone(id: Int, done: Boolean) {
        doneListener.setDone(id, done)
    }

    override fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int, category: Int) {
        showSubtasksListener.showSubtasks(subtasks, itemTitle, id, category)
    }

    interface ShowSubtasksListener {
        fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int, category: Int)
    }

    override fun calendarItemClicked(id: Int) {
        calendarItemClickListener.calendarItemClicked(id)
    }

    interface CalendarItemClickListener {
        fun calendarItemClicked(id: Int)
    }

}
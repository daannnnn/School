package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.School
import com.dan.school.fragments.CalendarFragment
import com.dan.school.models.CategoryEventList

class ParentEventListAdapter(
    private val events: ArrayList<CategoryEventList>,
    private val context: Context
) :
    RecyclerView.Adapter<ParentEventListAdapter.EventViewHolder>() {

    class EventViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recyclerViewEventsChild: RecyclerView = view.findViewById(R.id.recyclerViewEventsChild)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_events_parent_recycler_view, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val childEventListAdapter = ChildEventListAdapter(events[position].events, context)
        holder.recyclerViewEventsChild.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = childEventListAdapter
        }
    }
}
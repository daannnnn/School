package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.models.Item

class HomeworkListAdapter(private val context: Context) :
    RecyclerView.Adapter<HomeworkListAdapter.HomeworkViewHolder>() {

    private var homeworks = emptyList<Item>()

    class HomeworkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewItem: TextView = view.findViewById(R.id.textViewItem)
        val buttonCheckHomework: ImageButton = view.findViewById(R.id.buttonCheckHomework)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_homework, parent, false)
        return HomeworkViewHolder(view)
    }

    override fun getItemCount(): Int {
        return homeworks.size
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        holder.textViewItem.text = homeworks[position].title
    }

    fun setHomeworks(homeworks: List<Item>) {
        this.homeworks = homeworks
        notifyDataSetChanged()
    }
}
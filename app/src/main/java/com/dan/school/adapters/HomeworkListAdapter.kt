package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.fragments.SubtasksBottomSheetDialogFragment
import com.dan.school.models.Item
import com.dan.school.models.Subtask

class HomeworkListAdapter(
    private val context: Context,
    private val doneListener: DoneListener,
    private val showSubtasksListener: ShowSubtasksListener
) :
    ListAdapter<Item, HomeworkListAdapter.HomeworkViewHolder>(DIFF_CALLBACK) {

    class HomeworkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewItem: TextView = view.findViewById(R.id.textViewItem)
        val buttonCheckHomework: ImageButton = view.findViewById(R.id.buttonCheckHomework)
        val buttonSubtask: ImageButton = view.findViewById(R.id.buttonSubtask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_homework, parent, false)
        return HomeworkViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        holder.textViewItem.text = getItem(position).title
        if (getItem(holder.adapterPosition).done) {
            holder.buttonCheckHomework.setImageResource(R.drawable.ic_homework_checked)
        } else {
            holder.buttonCheckHomework.setImageResource(R.drawable.ic_homework_unchecked)
        }
        if (getItem(holder.adapterPosition).subtasks.isEmpty()) {
            holder.buttonSubtask.visibility = View.GONE
        } else {
            holder.buttonSubtask.visibility = View.VISIBLE
        }
        holder.buttonCheckHomework.setOnClickListener {
            holder.buttonCheckHomework.setImageResource(R.drawable.ic_homework_checked)
            doneListener.setDone(getItem(holder.adapterPosition).uid, true)
        }
        holder.buttonSubtask.setOnClickListener {
            val item = getItem(holder.adapterPosition)
            showSubtasksListener.showSubtasks(item.subtasks, item.title, item.uid)
        }
    }

    interface DoneListener {
        fun setDone(id: Int, done: Boolean)
    }

    interface ShowSubtasksListener {
        fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Item> =
            object : DiffUtil.ItemCallback<Item>() {
                override fun areItemsTheSame(
                    oldItem: Item, newItem: Item
                ): Boolean {
                    return oldItem.uid == newItem.uid
                }

                override fun areContentsTheSame(
                    oldItem: Item, newItem: Item
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
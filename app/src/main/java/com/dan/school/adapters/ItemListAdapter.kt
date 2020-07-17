package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.models.Item
import com.dan.school.models.Subtask

class ItemListAdapter(
    private val context: Context,
    private val doneListener: DoneListener,
    private val showSubtasksListener: ShowSubtasksListener,
    private val itemClickListener: ItemClickListener,
    private val uncheckedIcon: Int,
    private val checkedIcon: Int
) :
    ListAdapter<Item, ItemListAdapter.ItemViewHolder>(DIFF_CALLBACK) {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewItem: TextView = view.findViewById(R.id.textViewItem)
        val buttonCheckItem: ImageButton = view.findViewById(R.id.buttonCheck)
        val buttonSubtask: ImageButton = view.findViewById(R.id.buttonSubtask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textViewItem.text = getItem(position).title
        holder.itemView.setOnClickListener {
            itemClickListener.itemClicked(getItem(holder.adapterPosition))
        }
        if (getItem(holder.adapterPosition).done) {
            holder.buttonCheckItem.setImageResource(checkedIcon)
        } else {
            holder.buttonCheckItem.setImageResource(uncheckedIcon)
        }
        if (getItem(holder.adapterPosition).subtasks.isEmpty()) {
            holder.buttonSubtask.visibility = View.GONE
        } else {
            holder.buttonSubtask.visibility = View.VISIBLE
        }
        holder.buttonCheckItem.setOnClickListener {
            holder.buttonCheckItem.setImageResource(checkedIcon)
            doneListener.setDone(getItem(holder.adapterPosition).id, true)
        }
        holder.buttonSubtask.setOnClickListener {
            val item = getItem(holder.adapterPosition)
            showSubtasksListener.showSubtasks(item.subtasks, item.title, item.id)
        }
    }

    interface DoneListener {
        fun setDone(id: Int, done: Boolean)
    }

    interface ShowSubtasksListener {
        fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int)
    }

    interface ItemClickListener {
        fun itemClicked(item: Item)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Item> =
            object : DiffUtil.ItemCallback<Item>() {
                override fun areItemsTheSame(
                    oldItem: Item, newItem: Item
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Item, newItem: Item
                ): Boolean {
                    return oldItem.category == newItem.category &&
                            oldItem.done == newItem.done &&
                            oldItem.title == newItem.title &&
                            oldItem.date == newItem.date &&
                            oldItem.reminders == newItem.reminders &&
                            oldItem.subtasks == newItem.subtasks &&
                            oldItem.notes == newItem.notes
                }
            }
    }
}
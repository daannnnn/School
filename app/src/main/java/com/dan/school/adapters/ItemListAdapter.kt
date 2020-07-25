package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dan.school.ItemClickListener
import com.dan.school.ItemViewHolder
import com.dan.school.R
import com.dan.school.models.Item
import com.dan.school.models.Subtask

class ItemListAdapter(
    private val context: Context,
    private val doneListener: DoneListener,
    private val showSubtasksListener: ShowSubtasksListener,
    private val itemClickListener: ItemClickListener
) :
    ListAdapter<Item, ItemViewHolder>(DIFF_CALLBACK) {

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
        val view = LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textViewItem.text = getItem(position).title
        holder.itemView.setOnClickListener {
            itemClickListener.itemClicked(getItem(holder.bindingAdapterPosition))
        }
        if (getItem(position).subtasks.isEmpty()) {
            holder.buttonSubtask.visibility = View.GONE
        } else {
            holder.buttonSubtask.visibility = View.VISIBLE
        }
        val mItem = getItem(position)
        val mItemCategory = mItem.category
        if (getItem(position).done) {
            holder.buttonCheckItem.setImageResource(categoryCheckedIcons[mItemCategory])
        } else {
            holder.buttonCheckItem.setImageResource(categoryUncheckedIcons[mItemCategory])
        }
        holder.buttonCheckItem.setOnClickListener {
            if (getItem(holder.bindingAdapterPosition).done) {
                holder.buttonCheckItem.setImageResource(categoryUncheckedIcons[mItemCategory])
                doneListener.setDone(getItem(holder.bindingAdapterPosition).id, false)
            } else {
                holder.buttonCheckItem.setImageResource(categoryCheckedIcons[mItemCategory])
                doneListener.setDone(getItem(holder.bindingAdapterPosition).id, true)
            }
        }
        holder.buttonSubtask.setOnClickListener {
            val item = getItem(holder.bindingAdapterPosition)
            showSubtasksListener.showSubtasks(item.subtasks, item.title, item.id, item.category)
        }
    }

    interface DoneListener {
        fun setDone(id: Int, done: Boolean)
    }

    interface ShowSubtasksListener {
        fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int, category: Int)
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
                            oldItem.subtasks == newItem.subtasks &&
                            oldItem.notes == newItem.notes
                }
            }
    }
}
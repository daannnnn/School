package com.dan.school.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dan.school.ItemClickListener
import com.dan.school.ItemViewHolder
import com.dan.school.R
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryUncheckedIcons
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import java.util.*
import kotlin.collections.ArrayList

class ItemListAdapter(
    private val context: Context,
    private val doneListener: DoneListener,
    private val showSubtasksListener: ShowSubtasksListener,
    private val itemClickListener: ItemClickListener,
    private val itemLongClickListener: ItemLongClickListener
) :
    ListAdapter<Item, ItemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textViewItem.text = getItem(position).title
        holder.textViewItem.setOnClickListener {
            itemClickListener.itemClicked(getItem(holder.bindingAdapterPosition))
        }
        holder.textViewItem.setOnLongClickListener {
            itemLongClickListener.itemLongClicked(
                getItem(holder.bindingAdapterPosition).title,
                getItem(holder.bindingAdapterPosition).id
            )
            return@setOnLongClickListener true
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
            if (holder.bindingAdapterPosition != -1) {
                if (getItem(holder.bindingAdapterPosition).done) {
                    holder.buttonCheckItem.setImageResource(categoryUncheckedIcons[mItemCategory])
                    doneListener.setDone(getItem(holder.bindingAdapterPosition).id, false, null)
                } else {
                    holder.buttonCheckItem.setImageResource(categoryCheckedIcons[mItemCategory])
                    doneListener.setDone(
                        getItem(holder.bindingAdapterPosition).id,
                        true,
                        Calendar.getInstance().timeInMillis
                    )
                }
            }
        }
        holder.buttonSubtask.setOnClickListener {
            val item = getItem(holder.bindingAdapterPosition)
            showSubtasksListener.showSubtasks(item.subtasks, item.title, item.id, item.category)
        }
    }

    fun allItemsDone(): Boolean {
        for (item in currentList) {
            if (!item.done) {
                return false
            }
        }
        return true
    }

    interface DoneListener {
        fun setDone(id: String, done: Boolean, doneTime: Long?)
    }

    interface ShowSubtasksListener {
        fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: String, category: Int)
    }

    interface ItemLongClickListener {
        fun itemLongClicked(title: String, id: String)
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
                            oldItem.getSubtasksString() == newItem.getSubtasksString() &&
                            oldItem.notes == newItem.notes
                }
            }
    }
}
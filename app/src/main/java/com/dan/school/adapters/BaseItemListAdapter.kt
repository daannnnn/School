package com.dan.school.adapters

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.interfaces.ItemClickListener
import com.dan.school.ItemViewHolder
import com.dan.school.School
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.dan.school.models.UpcomingItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseItemListAdapter<T>(
    private val doneListener: DoneListener,
    private val showSubtasksListener: ShowSubtasksListener,
    private val itemClickListener: ItemClickListener,
    private val itemLongClickListener: ItemLongClickListener,
    private val `class`: Class<T>,
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {

    fun bind(holder: ItemViewHolder, item: Item) {
        val get: () -> Item = if (`class` == Item::class.java) {
            { getItem(holder.bindingAdapterPosition) as Item }
        } else {
            { (getItem(holder.bindingAdapterPosition) as UpcomingItem).item }
        }

        holder.textViewItem.text = item.title
        holder.itemView.setOnClickListener {
            itemClickListener.itemClicked(get())
        }
        holder.itemView.setOnLongClickListener {
            itemLongClickListener.itemLongClicked(
                get().title,
                get().id
            )
            return@setOnLongClickListener true
        }
        val subtasks = (Gson().fromJson(
            item.subtasks,
            object : TypeToken<java.util.ArrayList<Subtask?>?>() {}.type
        ) as ArrayList<Subtask?>)
        if (subtasks.isEmpty()) {
            holder.buttonSubtask.visibility = View.GONE
            holder.textViewSubtaskCount.visibility = View.GONE
        } else {
            holder.buttonSubtask.visibility = View.VISIBLE
            var count = 0
            for (subtask in subtasks) {
                if (subtask != null && !subtask.done) {
                    count++
                }
            }
            if (count != 0) {
                holder.textViewSubtaskCount.visibility = View.VISIBLE
                holder.textViewSubtaskCount.text = count.toString()
            } else {
                holder.textViewSubtaskCount.visibility = View.GONE
            }
        }
        val mItemCategory = item.category
        if (item.done) {
            holder.buttonCheckItem.setImageResource(School.categoryCheckedIcons[mItemCategory])
        } else {
            holder.buttonCheckItem.setImageResource(School.categoryUncheckedIcons[mItemCategory])
        }
        holder.buttonCheckItem.setOnClickListener {
            if (holder.bindingAdapterPosition != -1) {
                if (get().done) {
                    holder.buttonCheckItem.setImageResource(School.categoryUncheckedIcons[mItemCategory])
                    doneListener.setDone(get().id, false, null)
                } else {
                    holder.buttonCheckItem.setImageResource(School.categoryCheckedIcons[mItemCategory])
                    doneListener.setDone(
                        get().id,
                        true,
                        Calendar.getInstance().timeInMillis
                    )
                }
            }
        }
        holder.buttonSubtask.setOnClickListener {
            val mItem = get()
            showSubtasksListener.showSubtasks(
                Gson().fromJson(
                    mItem.subtasks,
                    object : TypeToken<java.util.ArrayList<Subtask?>?>() {}.type
                ), mItem.title, mItem.id, mItem.category
            )
        }
    }

    interface DoneListener {
        fun setDone(id: Int, done: Boolean, doneTime: Long?)
    }

    interface ShowSubtasksListener {
        fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int, category: Int)
    }

    interface ItemLongClickListener {
        fun itemLongClicked(title: String, id: Int)
    }

}
package com.dan.school.adapters

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.interfaces.ItemClickListener
import com.dan.school.ItemViewHolder
import com.dan.school.School
import com.dan.school.Utils
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
        val subtasks = Utils.convertJsonToListOfSubtasks(item.subtasks)
        val undoneSubtasksCount = Utils.countUndoneSubtasks(subtasks)

        holder.buttonSubtask.isGone = subtasks.isEmpty()

        if (subtasks.isEmpty() || undoneSubtasksCount == 0) {
            holder.textViewSubtaskCount.isGone = true
        } else {
            holder.textViewSubtaskCount.isGone = false
            holder.textViewSubtaskCount.text = undoneSubtasksCount.toString()
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
                Utils.convertJsonToListOfSubtasks(mItem.subtasks),
                mItem.title,
                mItem.id,
                mItem.category
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
package com.dan.school.adapters

import android.app.LauncherActivity.ListItem
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.ItemClickListener
import com.dan.school.ItemViewHolder
import com.dan.school.R
import com.dan.school.School
import com.dan.school.models.Subtask
import com.dan.school.models.UpcomingDate
import com.dan.school.models.UpcomingItem
import com.dan.school.models.UpcomingListItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList


class UpcomingItemListAdapter(
    private val context: Context,
    private val doneListener: ItemListAdapter.DoneListener,
    private val showSubtasksListener: ItemListAdapter.ShowSubtasksListener,
    private val itemClickListener: ItemClickListener,
    private val itemLongClickListener: ItemListAdapter.ItemLongClickListener
) : ListAdapter<UpcomingListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            UpcomingListItem.TYPE_DATE -> {
                val itemView: View =
                    LayoutInflater.from(context).inflate(R.layout.layout_upcoming_date, parent, false)
                UpcomingDateViewHolder(itemView)
            }
            UpcomingListItem.TYPE_MORE -> {
                val itemView: View =
                    LayoutInflater.from(context).inflate(R.layout.layout_upcoming_more, parent, false)
                UpcomingMoreViewHolder(itemView)
            }
            else -> {
                val itemView: View =
                    LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false)
                ItemViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        if (type == UpcomingListItem.TYPE_DATE) {
            val upcomingDate = getItem(position) as UpcomingDate
            val upcomingDateViewHolder = holder as UpcomingDateViewHolder
            upcomingDateViewHolder.textViewDate.text = upcomingDate.getDateString()
        } else if (type == UpcomingListItem.TYPE_ITEM) {
            val upcomingItem = (getItem(position) as UpcomingItem).item
            val upcomingItemViewHolder = holder as ItemViewHolder

            upcomingItemViewHolder.textViewItem.text = upcomingItem.title
            upcomingItemViewHolder.itemView.setOnClickListener {
                itemClickListener.itemClicked((getItem(holder.bindingAdapterPosition) as UpcomingItem).item)
            }
            upcomingItemViewHolder.itemView.setOnLongClickListener {
                itemLongClickListener.itemLongClicked(
                    (getItem(holder.bindingAdapterPosition) as UpcomingItem).item.title,
                    (getItem(holder.bindingAdapterPosition) as UpcomingItem).item.id
                )
                return@setOnLongClickListener true
            }
            if ((Gson().fromJson(
                    upcomingItem.subtasks,
                    object : TypeToken<java.util.ArrayList<Subtask?>?>() {}.type
                ) as ArrayList<Subtask?>).isEmpty()
            ) {
                upcomingItemViewHolder.buttonSubtask.visibility = View.GONE
            } else {
                upcomingItemViewHolder.buttonSubtask.visibility = View.VISIBLE
            }
            val mItemCategory = upcomingItem.category
            if (upcomingItem.done) {
                upcomingItemViewHolder.buttonCheckItem.setImageResource(School.categoryCheckedIcons[mItemCategory])
            } else {
                upcomingItemViewHolder.buttonCheckItem.setImageResource(School.categoryUncheckedIcons[mItemCategory])
            }
            upcomingItemViewHolder.buttonCheckItem.setOnClickListener {
                if (holder.bindingAdapterPosition != -1) {
                    if ((getItem(holder.bindingAdapterPosition) as UpcomingItem).item.done) {
                        upcomingItemViewHolder.buttonCheckItem.setImageResource(School.categoryUncheckedIcons[mItemCategory])
                        doneListener.setDone(
                            (getItem(holder.bindingAdapterPosition) as UpcomingItem).item.id,
                            false,
                            null
                        )
                    } else {
                        upcomingItemViewHolder.buttonCheckItem.setImageResource(School.categoryCheckedIcons[mItemCategory])
                        doneListener.setDone(
                            (getItem(holder.bindingAdapterPosition) as UpcomingItem).item.id,
                            true,
                            Calendar.getInstance().timeInMillis
                        )
                    }
                }
            }
            upcomingItemViewHolder.buttonSubtask.setOnClickListener {
                val item = (getItem(holder.bindingAdapterPosition) as UpcomingItem).item
                showSubtasksListener.showSubtasks(
                    Gson().fromJson(
                        item.subtasks,
                        object : TypeToken<java.util.ArrayList<Subtask?>?>() {}.type
                    ), item.title, item.id, item.category
                )
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    class UpcomingDateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewDate: TextView = view.findViewById(R.id.textViewDate)
    }

    class UpcomingMoreViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<UpcomingListItem> =
            object : DiffUtil.ItemCallback<UpcomingListItem>() {
                override fun areItemsTheSame(
                    oldItem: UpcomingListItem, newItem: UpcomingListItem
                ): Boolean {
                    return if (oldItem.type == newItem.type) {
                        when (oldItem.type) {
                            UpcomingListItem.TYPE_DATE -> {
                                (oldItem as UpcomingDate).date == (newItem as UpcomingDate).date
                            }
                            UpcomingListItem.TYPE_MORE -> {
                                true
                            }
                            else -> {
                                (oldItem as UpcomingItem).item.id == (newItem as UpcomingItem).item.id
                            }
                        }
                    } else {
                        false
                    }
                }

                override fun areContentsTheSame(
                    oldItem: UpcomingListItem, newItem: UpcomingListItem
                ): Boolean {
                    if (oldItem.type == newItem.type) {
                        return when (oldItem.type) {
                            UpcomingListItem.TYPE_DATE -> {
                                (oldItem as UpcomingDate).date == (newItem as UpcomingDate).date
                            }
                            UpcomingListItem.TYPE_MORE -> {
                                true
                            }
                            else -> {
                                val upcomingOldItem = (oldItem as UpcomingItem).item
                                val upcomingNewItem = (newItem as UpcomingItem).item
                                upcomingOldItem.category == upcomingNewItem.category &&
                                        upcomingOldItem.done == upcomingNewItem.done &&
                                        upcomingOldItem.title == upcomingNewItem.title &&
                                        upcomingOldItem.date == upcomingNewItem.date &&
                                        upcomingOldItem.subtasks == upcomingNewItem.subtasks &&
                                        upcomingOldItem.notes == upcomingNewItem.notes
                            }
                        }
                    } else {
                        return false
                    }
                }
            }
    }
}
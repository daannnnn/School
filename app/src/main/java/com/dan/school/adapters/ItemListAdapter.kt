package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.interfaces.ItemClickListener
import com.dan.school.other.ItemViewHolder
import com.dan.school.R
import com.dan.school.models.Item

class ItemListAdapter(
    private val context: Context,
    doneListener: DoneListener,
    showSubtasksListener: ShowSubtasksListener,
    itemClickListener: ItemClickListener,
    itemLongClickListener: ItemLongClickListener
) : BaseItemListAdapter<Item>(
    doneListener,
    showSubtasksListener,
    itemClickListener,
    itemLongClickListener,
    Item::class.java,
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bind(holder as ItemViewHolder, getItem(position))
    }

    fun allItemsDone(): Boolean {
        for (item in currentList) {
            if (!item.done) {
                return false
            }
        }
        return true
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
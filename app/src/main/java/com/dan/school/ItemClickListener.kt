package com.dan.school

import com.dan.school.models.Item

/** Invoked when a RecyclerView item is clicked */
interface ItemClickListener {
    fun itemClicked(item: Item)
}
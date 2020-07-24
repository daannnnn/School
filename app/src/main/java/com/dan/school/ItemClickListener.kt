package com.dan.school

import com.dan.school.models.Item

interface ItemClickListener {
    fun itemClicked(item: Item)
}
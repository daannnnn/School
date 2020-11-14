package com.dan.school.models

import java.util.*

class UpcomingItem(val item: Item): UpcomingListItem() {
    override val type: Int
        get() = TYPE_ITEM
}
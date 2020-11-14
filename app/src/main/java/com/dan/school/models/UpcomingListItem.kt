package com.dan.school.models

abstract class UpcomingListItem {
    abstract val type: Int

    companion object {
        const val TYPE_DATE = 0
        const val TYPE_ITEM = 1
        const val TYPE_MORE = 2
    }
}
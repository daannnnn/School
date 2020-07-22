package com.dan.school.models

import com.dan.school.School

class EventList: ArrayList<Event>() {
    fun hasCategory(category: Int): Boolean {
        for (event in this) {
            if (event.category == category) {
                return true
            }
        }
        return false
    }
    fun getCategorySortedList(): ArrayList<CategoryEventList> {
        val homeworksList = ArrayList<Event>()
        val examsList = ArrayList<Event>()
        val tasksList = ArrayList<Event>()

        for (event in this) {
            when (event.category) {
                School.HOMEWORK -> {
                    homeworksList.add(event)
                }
                School.EXAM -> {
                    examsList.add(event)
                }
                School.TASK -> {
                    tasksList.add(event)
                }
            }
        }

        val lists = ArrayList<CategoryEventList>()
        if (homeworksList.isNotEmpty()) {
            lists.add(CategoryEventList(School.HOMEWORK, homeworksList))
        }
        if (examsList.isNotEmpty()) {
            lists.add(CategoryEventList(School.EXAM, examsList))
        }
        if (tasksList.isNotEmpty()) {
            lists.add(CategoryEventList(School.TASK, tasksList))
        }
        return lists
    }

    fun indexOfItemWithId(id: Int): Int {
        for (index in 0 until this.size) {
            if (this[index].id == id) {
                return index
            }
        }
        return -1
    }
}
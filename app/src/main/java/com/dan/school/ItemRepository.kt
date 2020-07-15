package com.dan.school

import com.dan.school.models.Item

class ItemRepository(private val itemDao: ItemDao) {
    fun insert(item: Item) {
        itemDao.insert(item)
    }
    fun delete(item: Item) {
        itemDao.delete(item)
    }
}
package com.dan.school

import androidx.lifecycle.LiveData
import com.dan.school.models.Item

class ItemRepository(private val itemDao: ItemDao) {

    val allHomeworks: LiveData<List<Item>> = itemDao.getAllUndoneDataByCategory(School.HOMEWORK)

    fun setDone(id: Int, done: Boolean) {
        itemDao.setDone(id, done)
    }
    fun insert(item: Item) {
        itemDao.insert(item)
    }
    fun delete(item: Item) {
        itemDao.delete(item)
    }
}
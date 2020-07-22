package com.dan.school

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dan.school.models.DateItem
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import kotlin.collections.ArrayList

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAll(): List<Item>

    @Query("SELECT * FROM items WHERE category=:category AND done=0")
    fun getAllUndoneDataByCategory(category: Int): LiveData<List<Item>>

    @Query("UPDATE items SET done = :done WHERE id=:id")
    fun setDone(id: Int, done: Boolean)

    @Query("UPDATE items SET subtasks = :subtasks WHERE id=:id")
    fun setItemSubtasks(id: Int, subtasks: ArrayList<Subtask>)

    @Query("SELECT date, id, title, subtasks, done from items WHERE category=:category")
    fun getAllItemOnDateByCategory(category: Int): LiveData<List<DateItem>>

    @Insert
    fun insert(item: Item)

    @Update
    fun update(item: Item)

    @Delete
    fun delete(item: Item)
}
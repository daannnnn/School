package com.dan.school

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dan.school.models.Item
import com.dan.school.models.Subtask


@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAll(): List<Item>

    @Query("SELECT * FROM items WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Item>

    @Query("SELECT * FROM items WHERE category=:category AND done=0")
    fun getAllUndoneDataByCategory(category: Int): LiveData<List<Item>>

    @Query("UPDATE items SET done = :done WHERE uid=:id")
    fun setDone(id: Int, done: Boolean)

    @Query("UPDATE items SET subtasks = :subtasks WHERE uid=:id")
    fun setItemSubtasks(id: Int, subtasks: ArrayList<Subtask>)

    @Insert
    fun insert(user: Item)

    @Delete
    fun delete(user: Item)
}
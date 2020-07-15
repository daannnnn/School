package com.dan.school

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dan.school.models.Item


@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAll(): List<Item>

    @Query("SELECT * FROM items WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Item>

    @Query("SELECT * FROM items WHERE category=:category")
    fun getAllDataByCategory(category: Int): LiveData<List<Item>>

    @Insert
    fun insert(user: Item)

    @Delete
    fun delete(user: Item)
}
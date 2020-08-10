package com.dan.school

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dan.school.models.Item
import java.util.*

@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    fun getAll(): List<Item>

    @Query("SELECT * FROM items WHERE category=:category AND done=0")
    fun getAllUndoneDataByCategory(category: Int): LiveData<List<Item>>

    @Query("UPDATE items SET done = :done, doneTime = :doneTime WHERE id=:id")
    fun setDone(id: Int, done: Boolean, doneTime: Long?)

    @Query("UPDATE items SET subtasks = :subtasks WHERE id=:id")
    fun setItemSubtasks(id: Int, subtasks: String)

    @Query("SELECT date, id, title, subtasks, done from items WHERE category=:category")
    fun getAllItemOnDateByCategory(category: Int): LiveData<List<Date>>

    @Query("SELECT * from items WHERE date=:date AND category=${School.HOMEWORK}")
    fun getAllHomeworkByDate(date: Int): LiveData<List<Item>>

    @Query("SELECT * from items WHERE date=:date AND category=${School.EXAM}")
    fun getAllExamByDate(date: Int): LiveData<List<Item>>

    @Query("SELECT * from items WHERE date=:date AND category=${School.TASK}")
    fun getAllTaskByDate(date: Int): LiveData<List<Item>>

    @Query("SELECT * from items WHERE date<:date AND done=0")
    fun getAllOverdueItemsByDate(date: Int): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE id=:id")
    suspend fun getItemById(id: Int): Item

    @Query("SELECT EXISTS(SELECT * FROM items WHERE date = :date)")
    fun hasItemsForDate(date: Int): Boolean

    @Query("DELETE FROM items WHERE id = :id")
    fun deleteItemWithId(id: Int)

    @Insert
    fun insert(item: Item)

    @Update
    fun update(item: Item)
}
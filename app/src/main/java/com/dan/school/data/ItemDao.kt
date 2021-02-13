package com.dan.school.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.dan.school.School
import com.dan.school.models.DateItem
import com.dan.school.models.Item


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

    @Query("SELECT id, date from items WHERE category=:category")
    fun getAllItemOnDateByCategory(category: Int): LiveData<List<DateItem>>

    @Query("SELECT * from items WHERE date=:date AND category=${School.HOMEWORK}")
    fun getAllHomeworkByDate(date: Int): LiveData<List<Item>>

    @Query("SELECT * from items WHERE date=:date AND category=${School.EXAM}")
    fun getAllExamByDate(date: Int): LiveData<List<Item>>

    @Query("SELECT * from items WHERE date=:date AND category=${School.TASK}")
    fun getAllTaskByDate(date: Int): LiveData<List<Item>>

    @Query("SELECT * from items WHERE date<:date AND done=0")
    fun getAllOverdueItemsByDate(date: Int): LiveData<List<Item>>

    @RawQuery(observedEntities = [Item::class])
    fun runtimeQuery(sortQuery: SupportSQLiteQuery): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE id=:id")
    suspend fun getItemById(id: Int): Item

    @Query("SELECT EXISTS(SELECT * FROM items WHERE date = :date)")
    fun hasItemsForDate(date: Int): Boolean

    @Query("DELETE FROM items WHERE id = :id")
    fun deleteItemWithId(id: Int)

    @Query("SELECT DISTINCT date FROM items WHERE date > :date ORDER BY date ASC LIMIT 4")
    fun getDistinctDatesGreaterThan(date: Int): LiveData<List<Int>>

    @Query("SELECT * FROM items WHERE date > :date ORDER BY date ASC, id ASC LIMIT 11")
    fun getUpcomingItems(date: Int): LiveData<List<Item>>

    @Insert
    fun insert(item: Item)

    @Update
    fun update(item: Item)

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}
package com.dan.school

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SupportSQLiteQuery
import com.dan.school.models.DateItem
import com.dan.school.models.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val itemDao: ItemDao) {

    val allHomeworks: LiveData<List<Item>> = itemDao.getAllUndoneDataByCategory(School.HOMEWORK)
    val allExams: LiveData<List<Item>> = itemDao.getAllUndoneDataByCategory(School.EXAM)
    val allTasks: LiveData<List<Item>> = itemDao.getAllUndoneDataByCategory(School.TASK)
    val homeworkAllDates: LiveData<List<DateItem>> = itemDao.getAllItemOnDateByCategory(School.HOMEWORK)
    val examAllDates: LiveData<List<DateItem>> = itemDao.getAllItemOnDateByCategory(School.EXAM)
    val taskAllDates: LiveData<List<DateItem>> = itemDao.getAllItemOnDateByCategory(School.TASK)

    fun setDone(id: Int, done: Boolean, doneTime: Long?) {
        itemDao.setDone(id, done, doneTime)
    }
    fun setItemSubtasks(id: Int, subtasks: String) {
        itemDao.setItemSubtasks(id, subtasks)
    }
    fun insert(item: Item) {
        itemDao.insert(item)
    }
    fun update(item: Item) {
        itemDao.update(item)
    }
    fun deleteItemWithId(id: Int) {
        itemDao.deleteItemWithId(id)
    }
    suspend fun getAllHomeworkByDate(date: Int) = withContext(Dispatchers.IO) {
        itemDao.getAllHomeworkByDate(date)
    }
    suspend fun getAllExamByDate(date: Int) = withContext(Dispatchers.IO) {
        itemDao.getAllExamByDate(date)
    }
    suspend fun getAllTaskByDate(date: Int) = withContext(Dispatchers.IO) {
        itemDao.getAllTaskByDate(date)
    }
    suspend fun getAllOverdueItemsByDate(date: Int) = withContext(Dispatchers.IO) {
        itemDao.getAllOverdueItemsByDate(date)
    }
    suspend fun getUpcomingItems(date: Int) = withContext(Dispatchers.IO) {
        itemDao.getUpcomingItems(date)
    }
    suspend fun hasItemsForDate(date: Int) : Boolean = withContext(Dispatchers.IO) {
        itemDao.hasItemsForDate(date)
    }
    suspend fun runtimeQuery(query: SupportSQLiteQuery) = withContext(Dispatchers.IO) {
        itemDao.runtimeQuery(query)
    }
}
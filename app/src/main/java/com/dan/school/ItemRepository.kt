package com.dan.school

import androidx.lifecycle.LiveData
import com.dan.school.models.DateItem
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class ItemRepository(private val itemDao: ItemDao) {

    val allHomeworks: LiveData<List<Item>> = itemDao.getAllUndoneDataByCategory(School.HOMEWORK)
    val allExams: LiveData<List<Item>> = itemDao.getAllUndoneDataByCategory(School.EXAM)
    val allTasks: LiveData<List<Item>> = itemDao.getAllUndoneDataByCategory(School.TASK)
    val homeworkAllDates: LiveData<List<DateItem>> = itemDao.getAllItemOnDateByCategory(School.HOMEWORK)
    val examAllDates: LiveData<List<DateItem>> = itemDao.getAllItemOnDateByCategory(School.EXAM)
    val taskAllDates: LiveData<List<DateItem>> = itemDao.getAllItemOnDateByCategory(School.TASK)

    fun setDone(id: Int, done: Boolean) {
        itemDao.setDone(id, done)
    }
    fun setItemSubtasks(id: Int, subtasks: ArrayList<Subtask>) {
        itemDao.setItemSubtasks(id, subtasks)
    }
    fun insert(item: Item) {
        itemDao.insert(item)
    }
    fun update(item: Item) {
        itemDao.update(item)
    }
    fun delete(item: Item) {
        itemDao.delete(item)
    }
    suspend fun getItemById(id: Int) = withContext(Dispatchers.IO) {
        itemDao.getItemById(id)
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
}
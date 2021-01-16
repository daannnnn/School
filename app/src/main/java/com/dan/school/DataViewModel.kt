package com.dan.school

import android.app.Application
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.dan.school.models.DateItem
import com.dan.school.models.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val itemRepository: ItemRepository

    val allHomeworks: LiveData<List<Item>>
    val allExams: LiveData<List<Item>>
    val allTasks: LiveData<List<Item>>
    val homeworkAllDates: LiveData<List<DateItem>>
    val examAllDates: LiveData<List<DateItem>>
    val taskAllDates: LiveData<List<DateItem>>

    private val calendarSelectedDate = MutableLiveData<Int>()
    private val sortBy = MutableLiveData(School.DONE_TIME)

    init {
        val itemsDao = ItemDatabase.getInstance(application).itemDao()
        itemRepository = ItemRepository(itemsDao)
        allHomeworks = itemRepository.allHomeworks
        allExams = itemRepository.allExams
        allTasks = itemRepository.allTasks
        homeworkAllDates = itemRepository.homeworkAllDates
        examAllDates = itemRepository.examAllDates
        taskAllDates = itemRepository.taskAllDates
    }

    fun setCalendarSelectedDate(date: Int) {
        calendarSelectedDate.value = date
    }

    private val homeworks: LiveData<List<Item>> =
        Transformations.switchMap(
            calendarSelectedDate
        ) { date: Int ->
            return@switchMap getAllHomeworkByDate(date)
        }

    fun getCalendarHomeworks(): LiveData<List<Item>> {
        return homeworks
    }

    private val exams: LiveData<List<Item>> =
        Transformations.switchMap(
            calendarSelectedDate
        ) { date: Int ->
            return@switchMap getAllExamByDate(date)
        }

    fun getCalendarExams(): LiveData<List<Item>> {
        return exams
    }

    private val tasks: LiveData<List<Item>> =
        Transformations.switchMap(
            calendarSelectedDate
        ) { date: Int ->
            return@switchMap getAllTaskByDate(date)
        }

    fun getCalendarTasks(): LiveData<List<Item>> {
        return tasks
    }

    private fun getQueryDoneItems(category: Int, orderByColumn: String): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            "SELECT * from items WHERE done=1 AND category=$category ORDER BY $orderByColumn ${if (orderByColumn == School.TITLE) "ASC" else "DESC"}"
        )
    }

    private fun getQueryDoneItems(orderByColumn: String): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            "SELECT * from items WHERE done=1 ORDER BY $orderByColumn ${if (orderByColumn == School.TITLE) "ASC" else "DESC"}"
        )
    }

    fun setSortBy(sortBy: String) {
        this.sortBy.value = sortBy
    }

    private val doneHomeworks: LiveData<List<Item>> =
        Transformations.switchMap(
            sortBy
        ) { sortBy: String ->
            return@switchMap runtimeQuery(getQueryDoneItems(School.HOMEWORK, sortBy))
        }

    fun getDoneHomeworks(): LiveData<List<Item>> {
        return doneHomeworks
    }

    private val doneExams: LiveData<List<Item>> =
        Transformations.switchMap(
            sortBy
        ) { sortBy: String ->
            return@switchMap runtimeQuery(getQueryDoneItems(School.EXAM, sortBy))
        }

    fun getDoneExams(): LiveData<List<Item>> {
        return doneExams
    }

    private val doneTasks: LiveData<List<Item>> =
        Transformations.switchMap(
            sortBy
        ) { sortBy: String ->
            return@switchMap runtimeQuery(getQueryDoneItems(School.TASK, sortBy))
        }

    fun getDoneTasks(): LiveData<List<Item>> {
        return doneTasks
    }

    private val doneItems: LiveData<List<Item>> =
        Transformations.switchMap(
            sortBy
        ) { sortBy: String ->
            return@switchMap runtimeQuery(getQueryDoneItems(sortBy))
        }

    fun getDoneItems(): LiveData<List<Item>> {
        return doneItems
    }

    fun setDone(id: Int, done: Boolean, doneTime: Long?) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.setDone(id, done, doneTime)
    }

    fun setItemSubtasks(id: Int, subtasks: String) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.setItemSubtasks(id, subtasks)
    }

    fun insert(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.insert(item)
    }

    fun update(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.update(item)
    }

    fun deleteItemWithId(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.deleteItemWithId(id)
    }

    fun getAllHomeworkByDate(date: Int): LiveData<List<Item>> = runBlocking {
        itemRepository.getAllHomeworkByDate(date)
    }

    fun getAllExamByDate(date: Int): LiveData<List<Item>> = runBlocking {
        itemRepository.getAllExamByDate(date)
    }

    fun getAllTaskByDate(date: Int): LiveData<List<Item>> = runBlocking {
        itemRepository.getAllTaskByDate(date)
    }

    fun getAllOverdueItemsByDate(date: Int): LiveData<List<Item>> = runBlocking {
        itemRepository.getAllOverdueItemsByDate(date)
    }

    fun getUpcomingItems(date: Int): LiveData<List<Item>> = runBlocking {
        itemRepository.getUpcomingItems(date)
    }

    fun hasItemsForDate(date: Int): Boolean = runBlocking {
        itemRepository.hasItemsForDate(date)
    }

    private fun runtimeQuery(query: SimpleSQLiteQuery): LiveData<List<Item>> = runBlocking {
        itemRepository.runtimeQuery(query)
    }

    fun checkpoint(): Int = runBlocking {
        itemRepository.checkpoint()
    }
}
package com.dan.school

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.dan.school.models.DateItem
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val itemRepository: ItemRepository

    val allHomeworks: LiveData<List<Item>>
    val allExams: LiveData<List<Item>>
    val allTasks: LiveData<List<Item>>
    val homeworkAllDates: LiveData<List<DateItem>>
    val examAllDates: LiveData<List<DateItem>>
    val taskAllDates: LiveData<List<DateItem>>

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

    fun setDone(id: Int, done: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.setDone(id, done)
    }

    fun setItemSubtasks(id: Int, subtasks: ArrayList<Subtask>) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.setItemSubtasks(id, subtasks)
    }

    fun insert(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.insert(item)
    }

    fun update(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.update(item)
    }

    fun updateItemSubtasks(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.insert(item)
    }

    fun delete(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.delete(item)
    }

    fun getItemById(id: Int): Item = runBlocking {
        itemRepository.getItemById(id)
    }
}
package com.dan.school

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.dan.school.models.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val itemRepository: ItemRepository

    val allHomeworks: LiveData<List<Item>>

    init {
        val itemsDao = ItemDatabase.getInstance(application).itemDao()
        itemRepository = ItemRepository(itemsDao)
        allHomeworks = itemRepository.allHomeworks
    }

    fun setDone(id: Int, done: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.setDone(id, done)
    }

    fun insert(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.insert(item)
    }

    fun delete(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.delete(item)
    }
}
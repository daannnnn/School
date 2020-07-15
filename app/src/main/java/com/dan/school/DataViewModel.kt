package com.dan.school

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dan.school.models.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val itemRepository: ItemRepository

    init {
        val itemsDao = ItemDatabase.getInstance(application).itemDao()
        itemRepository = ItemRepository(itemsDao)
    }

    fun insert(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        itemRepository.insert(item)
    }
}
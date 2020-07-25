package com.dan.school

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dan.school.models.Item

@Database(entities = [Item::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ItemDatabase : RoomDatabase(){
    abstract fun itemDao():ItemDao

    companion object{
        private var instance: ItemDatabase? = null
        fun getInstance(context: Context): ItemDatabase{
            if (instance == null){
                instance = Room.databaseBuilder(
                    context,
                    ItemDatabase::class.java,
                    "itemsDB")
                    .build()
            }
            return instance as ItemDatabase
        }
    }
}
package com.dan.school

import android.app.Application
import androidx.lifecycle.*
import com.dan.school.models.Item
import com.dan.school.models.Profile
import com.dan.school.models.Subtask
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DataViewModel(application: Application) : AndroidViewModel(application) {

    // to be changed
    val USER_ID = "USER_ID"

    private val db = Firebase.firestore

    private lateinit var homeworkItemsListener: ListenerRegistration
    private lateinit var examItemsListener: ListenerRegistration
    private lateinit var taskItemsListener: ListenerRegistration

    private lateinit var allHomeworksTodayListener: ListenerRegistration
    private lateinit var allExamsTodayListener: ListenerRegistration
    private lateinit var allTasksTodayListener: ListenerRegistration
    private lateinit var overdueItemsTodayListener: ListenerRegistration

    private lateinit var doneItemsListener: ListenerRegistration

    val allUndoneHomeworks: MutableLiveData<List<Item>> = MutableLiveData()
    val allUndoneExams: MutableLiveData<List<Item>> = MutableLiveData()
    val allUndoneTasks: MutableLiveData<List<Item>> = MutableLiveData()

    val homeworkAllDates: MutableLiveData<List<Date>> = MutableLiveData()
    val examAllDates: MutableLiveData<List<Date>> = MutableLiveData()
    val taskAllDates: MutableLiveData<List<Date>> = MutableLiveData()

    val allHomeworksToday: MutableLiveData<List<Item>> = MutableLiveData()
    val allExamsToday: MutableLiveData<List<Item>> = MutableLiveData()
    val allTasksToday: MutableLiveData<List<Item>> = MutableLiveData()
    val overdueItemsToday: MutableLiveData<List<Item>> = MutableLiveData()

    val doneItems: MutableLiveData<List<Item>> = MutableLiveData()

    val hasItemsForTomorrow: MutableLiveData<Boolean> = MutableLiveData()

    val profile: MutableLiveData<Profile> = MutableLiveData()

    var isListeningToHomeCalendarItems = false
    var isListeningToAgendaItems = false
    var isListeningToDoneItems = false

    var lastListeningTo: Int = -1

    private val sortBy = MutableLiveData(School.DONE_TIME)
    private val today =
        SimpleDateFormat(
            School.dateFormatOnDatabase,
            Locale.getDefault()
        ).format(Calendar.getInstance().time).toInt()
    private val tomorrow =
        SimpleDateFormat(
            School.dateFormatOnDatabase,
            Locale.getDefault()
        ).format(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time).toInt()

    init {
        getHasItemsForTomorrow()

        getProfile()
    }

    fun stopListening(listeners: Int) {
        when (listeners) {
            School.HOME_CALENDAR_ITEMS -> {
                if (this::homeworkItemsListener.isInitialized) {
                    homeworkItemsListener.remove()
                }
                if (this::examItemsListener.isInitialized) {
                    examItemsListener.remove()
                }
                if (this::taskItemsListener.isInitialized) {
                    taskItemsListener.remove()
                }
                isListeningToHomeCalendarItems = false
            }
            School.AGENDA_ITEMS -> {
                if (this::allHomeworksTodayListener.isInitialized) {
                    allHomeworksTodayListener.remove()
                }
                if (this::allExamsTodayListener.isInitialized) {
                    allExamsTodayListener.remove()
                }
                if (this::allTasksTodayListener.isInitialized) {
                    allTasksTodayListener.remove()
                }
                if (this::overdueItemsTodayListener.isInitialized) {
                    overdueItemsTodayListener.remove()
                }
                isListeningToAgendaItems = false
            }
            School.DONE_ITEMS -> {
                if (this::doneItemsListener.isInitialized) {
                    doneItemsListener.remove()
                }
                isListeningToDoneItems = false
            }
        }
    }

    fun startListening(listeners: Int) {
        when (listeners) {
            School.HOME_CALENDAR_ITEMS -> {
                if (!isListeningToHomeCalendarItems) {
                    homeworkItemsListener = getAllItemsWithCategory(School.HOMEWORK)
                    examItemsListener = getAllItemsWithCategory(School.EXAM)
                    taskItemsListener = getAllItemsWithCategory(School.TASK)
                    isListeningToHomeCalendarItems = true
                    lastListeningTo = School.HOME_CALENDAR_ITEMS
                }
            }
            School.AGENDA_ITEMS -> {
                if (!isListeningToAgendaItems) {
                    allHomeworksTodayListener = getAllItemsTodayByCategory(School.HOMEWORK)
                    allExamsTodayListener = getAllItemsTodayByCategory(School.EXAM)
                    allTasksTodayListener = getAllItemsTodayByCategory(School.TASK)
                    overdueItemsTodayListener = getAllOverdueItemsToday()
                    isListeningToAgendaItems = true
                    lastListeningTo = School.AGENDA_ITEMS
                }
            }
            School.DONE_ITEMS -> {
                if (!isListeningToDoneItems) {
                    doneItemsListener = getDoneItems()
                    isListeningToDoneItems = true
                }
            }
        }
    }

    private fun getProfile() {
        db.document("$USER_ID/profile")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val fullName = value.getString("fullName")
                    val nickname = value.getString("nickname")
                    profile.value = Profile(fullName ?: "", nickname ?: "")
                } else {
                    profile.value = Profile()
                }
            }
    }

    fun updateProfile(profile: Profile) {
        db.document("$USER_ID/profile")
            .set(profile)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

    private fun getAllItemsWithCategory(category: Int): ListenerRegistration {
        return db.collection("$USER_ID/itemData/items")
            .whereEqualTo("category", category)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val undoneList = ArrayList<Item>()
                val dates = ArrayList<Date>()
                for (document in value!!.documents) {
                    val item = document.toObject(Item::class.java)!!
                    if (!item.done) {
                        undoneList.add(item)
                    }
                    dates.add(
                        SimpleDateFormat(School.dateFormatOnDatabase, Locale.getDefault()).parse(
                            document.toObject(Item::class.java)!!.date.toString()
                        )!!
                    )
                }
                when (category) {
                    School.HOMEWORK -> {
                        allUndoneHomeworks.value = undoneList
                        homeworkAllDates.value = dates
                    }
                    School.EXAM -> {
                        allUndoneExams.value = undoneList
                        examAllDates.value = dates
                    }
                    School.TASK -> {
                        allUndoneTasks.value = undoneList
                        taskAllDates.value = dates
                    }
                }
            }
    }

    fun setSortBy(sortBy: String) {
        this.sortBy.value = sortBy
    }

    private fun getDoneItems(): ListenerRegistration {
        return db.collection("$USER_ID/itemData/items")
            .whereEqualTo("done", true)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val list = ArrayList<Item>()
                for (document in value!!.documents) {
                    list.add(document.toObject(Item::class.java)!!)
                }
                doneItems.value = list
            }
    }

    fun setDone(id: String, done: Boolean, doneTime: Long?) {
        db.collection("$USER_ID/itemData/items")
            .document(id)
            .update("done", done, "doneTime", doneTime)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }

    fun setItemSubtasks(id: String, subtasks: ArrayList<Subtask>) {
        db.collection("$USER_ID/itemData/items")
            .document(id)
            .update("subtasks", subtasks)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }

    fun insert(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        db.collection("$USER_ID/itemData/items")
            .add(item)
            .addOnSuccessListener { documentReference ->

            }
            .addOnFailureListener { e ->

            }
    }

    fun update(item: Item) = viewModelScope.launch(Dispatchers.IO) {
        db.collection("$USER_ID/itemData/items")
            .document(item.id)
            .set(item)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }

    fun deleteItemWithId(id: String) = viewModelScope.launch(Dispatchers.IO) {
        db.collection("$USER_ID/itemData/items")
            .document(id)
            .delete()
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }

    private fun getAllItemsTodayByCategory(category: Int): ListenerRegistration {
        return db.collection("$USER_ID/itemData/items")
            .whereEqualTo("date", today)
            .whereEqualTo("category", category)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val list = ArrayList<Item>()
                for (document in value!!.documents) {
                    list.add(document.toObject(Item::class.java)!!)
                }
                when (category) {
                    School.HOMEWORK -> {
                        allHomeworksToday.value = list
                    }
                    School.EXAM -> {
                        allExamsToday.value = list
                    }
                    School.TASK -> {
                        allTasksToday.value = list
                    }
                }
            }
    }

    private fun getAllOverdueItemsToday(): ListenerRegistration {
        return db.collection("$USER_ID/itemData/items")
            .whereLessThan("date", today)
            .whereEqualTo("done", false)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val list = ArrayList<Item>()
                for (document in value!!.documents) {
                    list.add(document.toObject(Item::class.java)!!)
                }
                overdueItemsToday.value = list
            }
    }

    fun getHasItemsForTomorrow() {
        db.collection("$USER_ID/itemData/items")
            .whereEqualTo("date", tomorrow)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                hasItemsForTomorrow.value = value!!.documents.isNotEmpty()
            }
    }

    companion object {
        private const val TAG = "DataViewModel"
    }
}
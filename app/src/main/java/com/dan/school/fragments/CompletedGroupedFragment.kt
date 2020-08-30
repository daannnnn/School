package com.dan.school.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.*
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import kotlinx.android.synthetic.main.fragment_completed_grouped.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryUncheckedIcons
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList

class CompletedGroupedFragment : Fragment(), ItemListAdapter.DoneListener,
    ItemListAdapter.ShowSubtasksListener, ItemClickListener, ItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private val dataViewModel: DataViewModel by activityViewModels()

    private val doneHomeworks: MutableLiveData<List<Item>> = MutableLiveData()
    private val doneExams: MutableLiveData<List<Item>> = MutableLiveData()
    private val doneTasks: MutableLiveData<List<Item>> = MutableLiveData()

    private lateinit var doneHomeworksListener: ListenerRegistration
    private lateinit var doneExamsListener: ListenerRegistration
    private lateinit var doneTasksListener: ListenerRegistration

    private lateinit var homeworkListAdapter: ItemListAdapter
    private lateinit var examListAdapter: ItemListAdapter
    private lateinit var taskListAdapter: ItemListAdapter

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_completed_grouped, container, false)
    }

    override fun onStart() {
        super.onStart()
        doneHomeworksListener = getCompletedItemsByCategory(School.HOMEWORK)
        doneExamsListener = getCompletedItemsByCategory(School.EXAM)
        doneTasksListener = getCompletedItemsByCategory(School.TASK)
    }

    override fun onStop() {
        super.onStop()
        if (this::doneHomeworksListener.isInitialized) {
            doneHomeworksListener.remove()
        }
        if (this::doneExamsListener.isInitialized) {
            doneExamsListener.remove()
        }
        if (this::doneTasksListener.isInitialized) {
            doneTasksListener.remove()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeworkListAdapter = ItemListAdapter(
            requireContext(),
            this,
            this,
            this,
            this
        )
        examListAdapter = ItemListAdapter(
            requireContext(),
            this,
            this,
            this,
            this
        )
        taskListAdapter = ItemListAdapter(
            requireContext(),
            this,
            this,
            this,
            this
        )

        recyclerViewHomeworks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeworkListAdapter
        }
        recyclerViewExams.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = examListAdapter
        }
        recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskListAdapter
        }

        doneHomeworks
            .observe(viewLifecycleOwner, Observer { homeworks ->
                if (homeworks.isEmpty()) {
                    groupHomework.visibility = View.GONE
                } else {
                    groupHomework.visibility = View.VISIBLE
                }
                homeworkListAdapter.submitList(homeworks)
            })

        doneExams
            .observe(viewLifecycleOwner, Observer { exams ->
                if (exams.isEmpty()) {
                    groupExam.visibility = View.GONE
                } else {
                    groupExam.visibility = View.VISIBLE
                }
                examListAdapter.submitList(exams)
            })

        doneTasks
            .observe(viewLifecycleOwner, Observer { tasks ->
                if (tasks.isEmpty()) {
                    groupTask.visibility = View.GONE
                } else {
                    groupTask.visibility = View.VISIBLE
                }
                taskListAdapter.submitList(tasks)
            })
    }

    fun getCompletedItemsByCategory(category: Int): ListenerRegistration {
        return db.collection("${dataViewModel.USER_ID}/itemData/items")
            .whereEqualTo("category", category)
            .whereEqualTo("done", true)
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
                        doneHomeworks.value = list
                    }
                    School.EXAM -> {
                        doneExams.value = list
                    }
                    School.TASK -> {
                        doneTasks.value = list
                    }
                }
            }
    }

    override fun setDone(id: String, done: Boolean, doneTime: Long?) {
        dataViewModel.setDone(id, done, doneTime)
    }

    override fun showSubtasks(
        subtasks: ArrayList<Subtask>,
        itemTitle: String,
        id: String,
        category: Int
    ) {
        SubtasksBottomSheetDialogFragment(
            subtasks,
            itemTitle,
            id,
            categoryUncheckedIcons[category],
            categoryCheckedIcons[category]
        ).show(
            childFragmentManager,
            "subtasksBottomSheet"
        )
    }

    override fun itemClicked(item: Item) {
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat(
            School.dateFormatOnDatabase,
            Locale.getDefault()
        ).parse(item.date.toString())!!
        showEditFragment(
            item.category,
            item.done,
            item.doneTime,
            item.title,
            item.subtasks,
            item.notes,
            calendar,
            item.id
        )
    }

    override fun itemLongClicked(title: String, id: String) {
        ConfirmDeleteDialogFragment(this, id, title)
            .show(childFragmentManager, "confirmDeleteDialog")
    }

    override fun confirmDelete(itemId: String) {
        dataViewModel.deleteItemWithId(itemId)
    }

    private fun showEditFragment(
        category: Int,
        done: Boolean,
        doneTime: Long? = null,
        title: String,
        subtasks: java.util.ArrayList<Subtask>,
        notes: String,
        date: Calendar?,
        itemId: String
    ) {
        val editFragment = EditFragment.newInstance(
            category = category,
            done = done,
            doneTime = doneTime,
            title = title,
            subtasks = subtasks,
            notes = notes,
            chipGroupSelected = School.PICK_DATE,
            selectedDate = date,
            isEdit = true,
            itemId = itemId
        )
        editFragment.show(childFragmentManager, "editFragment")
    }

}
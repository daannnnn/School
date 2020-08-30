package com.dan.school.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.*
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryUncheckedIcons
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_agenda_tomorrow.*
import java.text.SimpleDateFormat
import java.util.*

class AgendaTomorrowFragment : DialogFragment(),
    ItemListAdapter.DoneListener,
    ItemListAdapter.ShowSubtasksListener, ItemClickListener, ItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private lateinit var itemClickListener: ItemClickListener

    private val dateTomorrow = Calendar.getInstance()

    private lateinit var homeworkListAdapter: ItemListAdapter
    private lateinit var examListAdapter: ItemListAdapter
    private lateinit var taskListAdapter: ItemListAdapter

    private val dataViewModel: DataViewModel by activityViewModels()

    private val allHomeworksTomorrow: MutableLiveData<List<Item>> = MutableLiveData()
    private val allExamsTomorrow: MutableLiveData<List<Item>> = MutableLiveData()
    private val allTasksTomorrow: MutableLiveData<List<Item>> = MutableLiveData()

    private lateinit var allHomeworksTomorrowListener: ListenerRegistration
    private lateinit var allExamsTomorrowListener: ListenerRegistration
    private lateinit var allTasksTomorrowListener: ListenerRegistration

    private var homeworkEmpty = false
    private var examEmpty = false
    private var taskEmpty = false

    private val db = Firebase.firestore

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is AgendaFragment) {
            itemClickListener = (parentFragment as AgendaFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        allHomeworksTomorrowListener = getAllItemsTomorrowByCategory(School.HOMEWORK)
        allExamsTomorrowListener = getAllItemsTomorrowByCategory(School.EXAM)
        allTasksTomorrowListener = getAllItemsTomorrowByCategory(School.TASK)
    }

    override fun onStop() {
        super.onStop()
        if (this::allHomeworksTomorrowListener.isInitialized) {
            allHomeworksTomorrowListener.remove()
        }
        if (this::allExamsTomorrowListener.isInitialized) {
            allExamsTomorrowListener.remove()
        }
        if (this::allTasksTomorrowListener.isInitialized) {
            allTasksTomorrowListener.remove()
        }
    }

    private fun getAllItemsTomorrowByCategory(category: Int): ListenerRegistration {
        return db.collection("${dataViewModel.USER_ID}/itemData/items")
            .whereEqualTo(
                "date", SimpleDateFormat(
                    School.dateFormatOnDatabase,
                    Locale.getDefault()
                ).format(dateTomorrow.time).toInt()
            )
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
                        allHomeworksTomorrow.value = list
                    }
                    School.EXAM -> {
                        allExamsTomorrow.value = list
                    }
                    School.TASK -> {
                        allTasksTomorrow.value = list
                    }
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations =
            R.style.DialogAnimation
        View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialog
        )
        dateTomorrow.add(Calendar.DAY_OF_MONTH, 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda_tomorrow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewDate.text =
            SimpleDateFormat(
                School.displayDateFormat,
                Locale.getDefault()
            ).format(dateTomorrow.time)

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

        allHomeworksTomorrow.observe(
            viewLifecycleOwner,
            { homeworks ->
                if (homeworks.isEmpty()) {
                    groupHomework.visibility = View.GONE
                    homeworkEmpty = true
                } else {
                    groupHomework.visibility = View.VISIBLE
                    homeworkEmpty = false
                }
                dismissIfEmpty()
                homeworkListAdapter.submitList(homeworks)
            })

        allExamsTomorrow.observe(
            viewLifecycleOwner,
            { exams ->
                if (exams.isEmpty()) {
                    groupExam.visibility = View.GONE
                    examEmpty = true
                } else {
                    groupExam.visibility = View.VISIBLE
                    examEmpty = false
                }
                dismissIfEmpty()
                examListAdapter.submitList(exams)
            })

        constraintLayoutAgendaTomorrowMain.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        allTasksTomorrow.observe(
            viewLifecycleOwner,
            { tasks ->
                if (tasks.isEmpty()) {
                    groupTask.visibility = View.GONE
                    taskEmpty = true
                } else {
                    groupTask.visibility = View.VISIBLE
                    taskEmpty = false
                }
                dismissIfEmpty()
                taskListAdapter.submitList(tasks)
            })

        buttonBack.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Calls [dismiss] when no items are found
     * for this date
     */
    private fun dismissIfEmpty() {
        if (homeworkEmpty && examEmpty && taskEmpty) {
            dismiss()
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
        if (this::itemClickListener.isInitialized) {
            itemClickListener.itemClicked(item)
        }
    }

    override fun itemLongClicked(title: String, id: String) {
        ConfirmDeleteDialogFragment(this, id, title)
            .show(childFragmentManager, "confirmDeleteDialog")
    }

    override fun confirmDelete(itemId: String) {
        dataViewModel.deleteItemWithId(itemId)
    }
}
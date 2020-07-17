package com.dan.school.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.DataViewModel
import com.dan.school.R
import com.dan.school.School
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import kotlinx.android.synthetic.main.fragment_items.*

class ItemsFragment(private val category: Int, private val itemClickListener: ItemClickListener) :
    Fragment(),
    ItemListAdapter.DoneListener,
    ItemListAdapter.ShowSubtasksListener, ItemListAdapter.ItemClickListener {

    private lateinit var dataViewModel: DataViewModel
    private lateinit var itemListAdapter: ItemListAdapter

    private val categoryCheckedIcons = arrayOf(
        R.drawable.ic_homework_checked,
        R.drawable.ic_exam_checked,
        R.drawable.ic_task_checked
    )
    private val categoryUncheckedIcons = arrayOf(
        R.drawable.ic_homework_unchecked,
        R.drawable.ic_exam_unchecked,
        R.drawable.ic_task_unchecked
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemListAdapter = ItemListAdapter(
            requireContext(),
            this,
            this,
            this,
            categoryUncheckedIcons[category],
            categoryCheckedIcons[category]
        )
        recyclerViewItems.layoutManager = LinearLayoutManager(context)
        recyclerViewItems.adapter = itemListAdapter

        when (category) {
            School.HOMEWORK -> {
                dataViewModel.allHomeworks.observe(viewLifecycleOwner, Observer { homeworks ->
                    homeworks?.let { itemListAdapter.submitList(it) }
                })
            }
            School.EXAM -> {
                dataViewModel.allExams.observe(viewLifecycleOwner, Observer { exams ->
                    exams?.let { itemListAdapter.submitList(it) }
                })
            }
            School.TASK -> {
                dataViewModel.allTasks.observe(viewLifecycleOwner, Observer { tasks ->
                    tasks?.let { itemListAdapter.submitList(it) }
                })
            }
        }
    }

    override fun setDone(id: Int, done: Boolean) {
        dataViewModel.setDone(id, done)
    }

    override fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int) {
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
        itemClickListener.itemClicked(item)
    }

    interface ItemClickListener {
        fun itemClicked(item: Item)
    }
}
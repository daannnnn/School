package com.dan.school.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.DataViewModel
import com.dan.school.ItemClickListener
import com.dan.school.R
import com.dan.school.School
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryColors
import com.dan.school.School.categoryUncheckedIcons
import com.dan.school.adapters.BaseItemListAdapter
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import kotlinx.android.synthetic.main.fragment_items.*

class ItemsFragment : Fragment(),
    BaseItemListAdapter.DoneListener,
    BaseItemListAdapter.ShowSubtasksListener, ItemClickListener, BaseItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var itemListAdapter: ItemListAdapter

    private lateinit var itemClickListener: ItemClickListener

    private var category = 0

    private val categoryNoItemStrings = arrayOf(
        R.string.no_homeworks,
        R.string.no_exams,
        R.string.no_tasks
    )
    private val categoryNoItemDrawables = arrayOf(
        R.drawable.ic_homework_inside_circle,
        R.drawable.ic_exam_inside_circle,
        R.drawable.ic_task_inside_circle
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is HomeFragment) {
            itemClickListener = (parentFragment as HomeFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = requireArguments().getInt("category", 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewNoItem.setText(categoryNoItemStrings[category])
        textViewNoItem.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                categoryColors[category]
            )
        )
        imageViewNoItem.setImageResource(categoryNoItemDrawables[category])

        itemListAdapter = ItemListAdapter(
            requireContext(),
            this,
            this,
            this,
            this
        )
        recyclerViewItems.layoutManager = LinearLayoutManager(context)
        recyclerViewItems.adapter = itemListAdapter

        when (category) {
            School.HOMEWORK -> {
                dataViewModel.allHomeworks.observe(viewLifecycleOwner, { homeworks ->
                    homeworks?.let { itemListAdapter.submitList(it) }
                    setVisibilities(homeworks.isEmpty())
                })
            }
            School.EXAM -> {
                dataViewModel.allExams.observe(viewLifecycleOwner, { exams ->
                    exams?.let { itemListAdapter.submitList(it) }
                    setVisibilities(exams.isEmpty())
                })
            }
            School.TASK -> {
                dataViewModel.allTasks.observe(viewLifecycleOwner, { tasks ->
                    tasks?.let { itemListAdapter.submitList(it) }
                    setVisibilities(tasks.isEmpty())
                })
            }
        }
    }

    override fun setDone(id: Int, done: Boolean, doneTime: Long?) {
        dataViewModel.setDone(id, done, doneTime)
    }

    override fun showSubtasks(
        subtasks: ArrayList<Subtask>,
        itemTitle: String,
        id: Int,
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

    override fun itemLongClicked(title: String, id: Int) {
        ConfirmDeleteDialogFragment(this, id, title)
            .show(childFragmentManager, "confirmDeleteDialog")
    }

    override fun confirmDelete(itemId: Int) {
        dataViewModel.deleteItemWithId(itemId)
    }

    fun setVisibilities(isEmpty: Boolean) {
        if (isEmpty) {
            if (linearLayoutNoItem.isGone) {
                recyclerViewItems.isVisible = false
                linearLayoutNoItem.isVisible = true
            }
        } else {
            if (linearLayoutNoItem.isVisible) {
                recyclerViewItems.isVisible = true
                linearLayoutNoItem.isVisible = false
            }
        }
    }

    companion object {
        fun newInstance(category: Int) = ItemsFragment().apply {
            arguments = bundleOf("category" to category)
        }
    }
}
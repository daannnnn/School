package com.dan.school.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.DataViewModel
import com.dan.school.ItemClickListener
import com.dan.school.School
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryUncheckedIcons
import com.dan.school.adapters.BaseItemListAdapter
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.databinding.FragmentCompletedGroupedBinding
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class CompletedGroupedFragment : Fragment(), BaseItemListAdapter.DoneListener,
    BaseItemListAdapter.ShowSubtasksListener, ItemClickListener, BaseItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private var _binding: FragmentCompletedGroupedBinding? = null

    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()

    private lateinit var homeworkListAdapter: ItemListAdapter
    private lateinit var examListAdapter: ItemListAdapter
    private lateinit var taskListAdapter: ItemListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedGroupedBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.recyclerViewHomeworks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeworkListAdapter
        }
        binding.recyclerViewExams.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = examListAdapter
        }
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskListAdapter
        }

        dataViewModel.getDoneHomeworks()
            .observe(viewLifecycleOwner, { homeworks ->
                if (homeworks.isEmpty()) {
                    binding.groupHomework.visibility = View.GONE
                } else {
                    binding.groupHomework.visibility = View.VISIBLE
                }
                homeworkListAdapter.submitList(homeworks)
            })

        dataViewModel.getDoneExams()
            .observe(viewLifecycleOwner, { exams ->
                if (exams.isEmpty()) {
                    binding.groupExam.visibility = View.GONE
                } else {
                    binding.groupExam.visibility = View.VISIBLE
                }
                examListAdapter.submitList(exams)
            })

        dataViewModel.getDoneTasks()
            .observe(viewLifecycleOwner, { tasks ->
                if (tasks.isEmpty()) {
                    binding.groupTask.visibility = View.GONE
                } else {
                    binding.groupTask.visibility = View.VISIBLE
                }
                taskListAdapter.submitList(tasks)
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            null
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
            Gson().fromJson(
                item.subtasks,
                object : TypeToken<ArrayList<Subtask?>?>() {}.type
            ),
            item.notes,
            calendar,
            item.id
        )
    }

    override fun itemLongClicked(title: String, id: Int) {
        ConfirmDeleteDialogFragment(this, id, title)
            .show(childFragmentManager, null)
    }

    override fun confirmDelete(itemId: Int) {
        dataViewModel.deleteItemWithId(itemId)
    }

    private fun showEditFragment(
        category: Int,
        done: Boolean,
        doneTime: Long? = null,
        title: String,
        subtasks: ArrayList<Subtask>,
        notes: String,
        date: Calendar?,
        itemId: Int
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
        editFragment.show(childFragmentManager, null)
    }

}
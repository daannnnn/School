package com.dan.school.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.*
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryUncheckedIcons
import com.dan.school.adapters.BaseItemListAdapter
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.databinding.FragmentAgendaTomorrowBinding
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import java.text.SimpleDateFormat
import java.util.*

class AgendaTomorrowFragment : DialogFragment(),
    BaseItemListAdapter.DoneListener,
    BaseItemListAdapter.ShowSubtasksListener, ItemClickListener, BaseItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private var _binding: FragmentAgendaTomorrowBinding? = null

    private val binding get() = _binding!!

    private lateinit var itemClickListener: ItemClickListener

    private val dateTomorrow = Calendar.getInstance()

    private lateinit var homeworkListAdapter: ItemListAdapter
    private lateinit var examListAdapter: ItemListAdapter
    private lateinit var taskListAdapter: ItemListAdapter

    private val dataViewModel: DataViewModel by activityViewModels()

    private var homeworkEmpty = false
    private var examEmpty = false
    private var taskEmpty = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is AgendaFragment) {
            itemClickListener = (parentFragment as AgendaFragment)
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
    ): View {
        _binding = FragmentAgendaTomorrowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewDate.text =
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

        dataViewModel.getAllHomeworkByDate(
            SimpleDateFormat(
                School.dateFormatOnDatabase,
                Locale.getDefault()
            ).format(dateTomorrow.time).toInt()
        ).observe(viewLifecycleOwner, { homeworks ->
            if (homeworks.isEmpty()) {
                binding.groupHomework.visibility = View.GONE
                homeworkEmpty = true
            } else {
                binding.groupHomework.visibility = View.VISIBLE
                homeworkEmpty = false
            }
            dismissIfEmpty()
            homeworkListAdapter.submitList(homeworks)
        })

        dataViewModel.getAllExamByDate(
            SimpleDateFormat(
                School.dateFormatOnDatabase,
                Locale.getDefault()
            ).format(dateTomorrow.time).toInt()
        ).observe(viewLifecycleOwner, { exams ->
            if (exams.isEmpty()) {
                binding.groupExam.visibility = View.GONE
                examEmpty = true
            } else {
                binding.groupExam.visibility = View.VISIBLE
                examEmpty = false
            }
            dismissIfEmpty()
            examListAdapter.submitList(exams)
        })

        binding.constraintLayoutAgendaTomorrowMain.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        dataViewModel.getAllTaskByDate(
            SimpleDateFormat(
                School.dateFormatOnDatabase,
                Locale.getDefault()
            ).format(dateTomorrow.time).toInt()
        ).observe(viewLifecycleOwner, { tasks ->
            if (tasks.isEmpty()) {
                binding.groupTask.visibility = View.GONE
                taskEmpty = true
            } else {
                binding.groupTask.visibility = View.VISIBLE
                taskEmpty = false
            }
            dismissIfEmpty()
            taskListAdapter.submitList(tasks)
        })

        binding.buttonBack.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        if (this::itemClickListener.isInitialized) {
            itemClickListener.itemClicked(item)
        }
    }

    override fun itemLongClicked(title: String, id: Int) {
        ConfirmDeleteDialogFragment(this, id, title)
            .show(childFragmentManager, null)
    }

    override fun confirmDelete(itemId: Int) {
        dataViewModel.deleteItemWithId(itemId)
    }
}
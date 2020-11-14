package com.dan.school.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.*
import com.dan.school.School.categoryCheckedIcons
import com.dan.school.School.categoryUncheckedIcons
import com.dan.school.adapters.BaseItemListAdapter
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.adapters.UpcomingItemListAdapter
import com.dan.school.models.*
import kotlinx.android.synthetic.main.fragment_agenda.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AgendaFragment : Fragment(),
    BaseItemListAdapter.DoneListener,
    BaseItemListAdapter.ShowSubtasksListener,
    ItemClickListener,
    BaseItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private lateinit var itemClickListener: ItemClickListener

    private val dataViewModel: DataViewModel by activityViewModels()

    private val dateToday = Calendar.getInstance()
    private val dateTomorrow = Calendar.getInstance()
    private val hourOfDay = dateToday.get(Calendar.HOUR_OF_DAY)

    private lateinit var overdueListAdapter: ItemListAdapter
    private lateinit var homeworkListAdapter: ItemListAdapter
    private lateinit var examListAdapter: ItemListAdapter
    private lateinit var taskListAdapter: ItemListAdapter
    private lateinit var upcomingItemListAdapter: UpcomingItemListAdapter

    private lateinit var sharedPref: SharedPreferences

    private var onSharedPreferenceChangeListener:
            SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        if (parentFragment is OverviewFragment) {
            itemClickListener = (parentFragment as OverviewFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateTomorrow.add(Calendar.DAY_OF_MONTH, 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == School.NICKNAME) {
                    updateGreeting(sharedPreferences.getString(School.NICKNAME, ""))
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)

        updateGreeting(sharedPref.getString(School.NICKNAME, ""))

        overdueListAdapter = ItemListAdapter(
            requireContext(),
            this,
            this,
            this,
            this
        )
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
        upcomingItemListAdapter = UpcomingItemListAdapter(
            requireContext(),
            this,
            this,
            this,
            this
        )

        recyclerViewOverdue.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = overdueListAdapter
        }
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
        recyclerViewUpcoming.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingItemListAdapter
        }

        val todayDateInt = SimpleDateFormat(
            School.dateFormatOnDatabase,
            Locale.getDefault()
        ).format(dateToday.time).toInt()

        dataViewModel.getAllOverdueItemsByDate(todayDateInt)
            .observe(viewLifecycleOwner, { overdueItems ->
                if (overdueItems.isEmpty()) {
                    groupOverdue.visibility = View.GONE
                } else {
                    groupOverdue.visibility = View.VISIBLE
                }
                overdueListAdapter.submitList(overdueItems) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getAllHomeworkByDate(todayDateInt)
            .observe(viewLifecycleOwner, { homeworks ->
                if (homeworks.isEmpty()) {
                    groupHomework.visibility = View.GONE
                } else {
                    groupHomework.visibility = View.VISIBLE
                }
                homeworkListAdapter.submitList(homeworks) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getAllExamByDate(todayDateInt)
            .observe(viewLifecycleOwner, { exams ->
                if (exams.isEmpty()) {
                    groupExam.visibility = View.GONE
                } else {
                    groupExam.visibility = View.VISIBLE
                }
                examListAdapter.submitList(exams) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getAllTaskByDate(todayDateInt)
            .observe(viewLifecycleOwner, { tasks ->
                if (tasks.isEmpty()) {
                    groupTask.visibility = View.GONE
                } else {
                    groupTask.visibility = View.VISIBLE
                }
                taskListAdapter.submitList(tasks) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getUpcomingItems(todayDateInt)
            .observe(viewLifecycleOwner, { upcomingItems ->
                if (upcomingItems.isEmpty()) {
                    groupUpcoming.visibility = View.GONE
                    viewDivider.visibility = View.GONE
                    upcomingItemListAdapter.submitList(ArrayList())
                } else {
                    groupUpcoming.visibility = View.VISIBLE
                    viewDivider.visibility = View.VISIBLE
                    updateUpcoming(upcomingItems)
                }
            })

        buttonSeeTomorrow.setOnClickListener {
            AgendaTomorrowFragment().show(childFragmentManager, "agendaTomorrowFragment")
        }

        textViewDate.text =
            SimpleDateFormat(School.displayDateFormat, Locale.getDefault()).format(dateToday.time)

        constraintLayoutAgendaMain.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun updateUpcoming(upcomingItems: List<Item>) {
        val upcomingItemsList = ArrayList<UpcomingListItem>()
        var count = 0
        var lastDate = upcomingItems[0].date
        upcomingItemsList.add(UpcomingDate(lastDate))
        for (item in upcomingItems) {
            if (count < 10) {
                if (item.date == lastDate) {
                    upcomingItemsList.add(UpcomingItem(item))
                } else {
                    lastDate = item.date
                    upcomingItemsList.add(UpcomingDate(lastDate))
                    upcomingItemsList.add(UpcomingItem(item))
                }
                count++
            } else {
                upcomingItemsList.add(UpcomingMore())
            }
        }
        upcomingItemListAdapter.submitList(upcomingItemsList)
    }

    /**
     * Updates [textViewGreeting] text with
     * appropriate greeting to given [nickname]
     */
    private fun updateGreeting(nickname: String?) {
        textViewGreeting.text = when (hourOfDay) {
            in 5..11 -> {
                "Good Morning, $nickname!"
            }
            in 12..16 -> {
                "Good Afternoon, $nickname!"
            }
            else -> {
                "Good Evening, $nickname!"
            }
        }
    }

    /**
     * Hides, shows and sets [textViewMessage] visibility
     * and text depending on list contents
     */
    private fun refreshShowEmptyMessageVisibility() {
        if (overdueListAdapter.currentList.isEmpty() &&
            homeworkListAdapter.currentList.isEmpty() &&
            examListAdapter.currentList.isEmpty() &&
            taskListAdapter.currentList.isEmpty()
        ) {
            textViewMessage.setText(R.string.you_don_t_have_anything_scheduled_for_today)
        } else if (overdueListAdapter.currentList.isEmpty() &&
            homeworkListAdapter.allItemsDone() &&
            examListAdapter.allItemsDone() &&
            taskListAdapter.allItemsDone()
        ) {
            textViewMessage.setText(R.string.all_done)
        } else {
            cardViewMessage.isGone = true
            return
        }
        cardViewMessage.isVisible = true

        buttonSeeTomorrow.isVisible = dataViewModel.hasItemsForDate(
            SimpleDateFormat(
                School.dateFormatOnDatabase,
                Locale.getDefault()
            ).format(dateTomorrow.time).toInt()
        )
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
}
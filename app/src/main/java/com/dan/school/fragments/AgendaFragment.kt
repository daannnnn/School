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
import com.dan.school.databinding.FragmentAgendaBinding
import com.dan.school.interfaces.ItemClickListener
import com.dan.school.models.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AgendaFragment : Fragment(),
    BaseItemListAdapter.DoneListener,
    BaseItemListAdapter.ShowSubtasksListener,
    ItemClickListener,
    BaseItemListAdapter.ItemLongClickListener,
    ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private var _binding: FragmentAgendaBinding? = null

    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentAgendaBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.recyclerViewOverdue.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = overdueListAdapter
        }
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
        binding.recyclerViewUpcoming.apply {
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
                    binding.groupOverdue.visibility = View.GONE
                } else {
                    binding.groupOverdue.visibility = View.VISIBLE
                }
                overdueListAdapter.submitList(overdueItems) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getAllHomeworkByDate(todayDateInt)
            .observe(viewLifecycleOwner, { homeworks ->
                if (homeworks.isEmpty()) {
                    binding.groupHomework.visibility = View.GONE
                } else {
                    binding.groupHomework.visibility = View.VISIBLE
                }
                homeworkListAdapter.submitList(homeworks) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getAllExamByDate(todayDateInt)
            .observe(viewLifecycleOwner, { exams ->
                if (exams.isEmpty()) {
                    binding.groupExam.visibility = View.GONE
                } else {
                    binding.groupExam.visibility = View.VISIBLE
                }
                examListAdapter.submitList(exams) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getAllTaskByDate(todayDateInt)
            .observe(viewLifecycleOwner, { tasks ->
                if (tasks.isEmpty()) {
                    binding.groupTask.visibility = View.GONE
                } else {
                    binding.groupTask.visibility = View.VISIBLE
                }
                taskListAdapter.submitList(tasks) {
                    refreshShowEmptyMessageVisibility()
                }
            })

        dataViewModel.getUpcomingItems(todayDateInt)
            .observe(viewLifecycleOwner, { upcomingItems ->
                if (upcomingItems.isEmpty()) {
                    binding.groupUpcoming.visibility = View.GONE
                    binding.viewDivider.visibility = View.GONE
                    upcomingItemListAdapter.submitList(ArrayList())
                } else {
                    binding.groupUpcoming.visibility = View.VISIBLE
                    binding.viewDivider.visibility = View.VISIBLE
                    updateUpcoming(upcomingItems)
                }
            })

        binding.buttonSeeTomorrow.setOnClickListener {
            AgendaTomorrowFragment().show(childFragmentManager, null)
        }

        binding.textViewDate.text =
            SimpleDateFormat(School.displayDateFormat, Locale.getDefault()).format(dateToday.time)

        binding.constraintLayoutAgendaMain.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
     * Updates [FragmentAgendaBinding.textViewGreeting] text with
     * appropriate greeting to given [nickname]
     */
    private fun updateGreeting(nickname: String?) {
        binding.textViewGreeting.text = when (hourOfDay) {
            in 5..11 -> {
                "${getString(R.string.good_morning)}, $nickname!"
            }
            in 12..16 -> {
                "${getString(R.string.good_afternoon)}, $nickname!"
            }
            else -> {
                "${getString(R.string.good_evening)}, $nickname!"
            }
        }
    }

    /**
     * Hides, shows and sets [FragmentAgendaBinding.textViewMessage] visibility
     * and text depending on list contents
     */
    private fun refreshShowEmptyMessageVisibility() {
        if (overdueListAdapter.currentList.isEmpty() &&
            homeworkListAdapter.currentList.isEmpty() &&
            examListAdapter.currentList.isEmpty() &&
            taskListAdapter.currentList.isEmpty()
        ) {
            binding.textViewMessage.setText(R.string.you_don_t_have_anything_scheduled_for_today)
        } else if (overdueListAdapter.currentList.isEmpty() &&
            homeworkListAdapter.allItemsDone() &&
            examListAdapter.allItemsDone() &&
            taskListAdapter.allItemsDone()
        ) {
            binding.textViewMessage.setText(R.string.all_done)
        } else {
            binding.cardViewMessage.isGone = true
            return
        }
        binding.cardViewMessage.isVisible = true

        binding.buttonSeeTomorrow.isVisible = dataViewModel.hasItemsForDate(
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
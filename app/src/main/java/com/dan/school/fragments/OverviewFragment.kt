package com.dan.school.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.interfaces.ItemClickListener
import com.dan.school.MainActivity
import com.dan.school.R
import com.dan.school.School
import com.dan.school.databinding.FragmentOverviewBinding
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class OverviewFragment : Fragment(),
    AddBottomSheetDialogFragment.GoToEditFragment,
    AddBottomSheetDialogFragment.SelectedCategoryChangeListener,
    EditFragment.CategoryChangeListener,
    EditFragment.DismissBottomSheetListener,
    ItemClickListener, CalendarFragment.TitleChangeListener,
    HomeFragment.SelectedTabChangeListener {

    private var _binding: FragmentOverviewBinding? = null

    private val binding get() = _binding!!

    private var addBottomSheetDialogFragment: AddBottomSheetDialogFragment? = null
    private var lastSelectedAddCategory = School.HOMEWORK
    private var isMonthView = true

    private var selectedFragment = 0

    private lateinit var openDrawerListener: OpenDrawerListener
    private lateinit var clickCounterListener: ClickCounterListener

    private lateinit var sharedPref: SharedPreferences

    private var canSelectItem = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        if (activity is MainActivity) {
            openDrawerListener = activity as MainActivity
            clickCounterListener = activity as MainActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedFragment =
            sharedPref.getInt(
                School.SELECTED_BOTTOM_NAVIGATION_FRAGMENT,
                School.HOME_SELECTED
            )

        if (savedInstanceState == null) {
            // Show last selected fragment saved on SharedPreferences
            when (selectedFragment) {
                School.HOME_SELECTED -> {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            HomeFragment(), School.HOME
                        ).commit()
                    binding.bottomNavigation.selectedItemId =
                        R.id.homeFragment
                }
                School.CALENDAR_SELECTED -> {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            CalendarFragment(), School.CALENDAR
                        ).commit()
                    binding.buttonCalendarView.visibility = View.VISIBLE
                    binding.bottomNavigation.selectedItemId =
                        R.id.calendarFragment
                }
                School.AGENDA_SELECTED -> {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            AgendaFragment(), School.AGENDA
                        ).commit()
                    binding.bottomNavigation.selectedItemId =
                        R.id.agendaFragment
                }
            }
        }

        setButtonCalendarViewBackground()
        if (selectedFragment == School.CALENDAR_SELECTED) {
            binding.buttonCalendarView.visibility = View.VISIBLE
        }

        // Listeners
        binding.floatingActionButton.setOnClickListener {
            clickCounterListener.incrementCounter {
                showAddBottomSheetDialog()
            }
        }
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            clickCounterListener.incrementCounter {
                when (item.itemId) {
                    R.id.homeFragment -> {
                        setFragment(School.HOME)
                        binding.textViewAppBarTitle.text = getString(R.string.app_name)
                    }
                    R.id.calendarFragment -> {
                        setFragment(School.CALENDAR)
                    }
                    R.id.agendaFragment -> {
                        setFragment(School.AGENDA)
                        binding.textViewAppBarTitle.text = getString(R.string.app_name)
                    }
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
        binding.bottomNavigation.setOnNavigationItemReselectedListener {}
        binding.buttonMenu.setOnClickListener {
            if (this::openDrawerListener.isInitialized) {
                openDrawerListener.openDrawer()
            }
        }
        binding.buttonCalendarView.setOnClickListener {
            if (childFragmentManager.findFragmentByTag(School.CALENDAR) != null) {
                if ((childFragmentManager.findFragmentByTag(School.CALENDAR) as CalendarFragment).setCalendarView(
                        isMonthView
                    )
                ) {
                    isMonthView = !isMonthView
                    setButtonCalendarViewBackground()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Creates and shows an [AddBottomSheetDialogFragment]
     */
    private fun showAddBottomSheetDialog() {

        /**
         * Used to set the selected date from [CalendarFragment] on
         * the [AddBottomSheetDialogFragment] if [CalendarFragment]
         * is the current fragment, otherwise the current date.
         *
         * [CalendarFragment.getSelectedDate] if [selectedFragment]
         * is [School.CALENDAR_SELECTED] and if fragment with tag
         * [School.CALENDAR] is not null, otherwise null.
         */
        val selectedCalendarDate =
            if (selectedFragment == School.CALENDAR_SELECTED && childFragmentManager.findFragmentByTag(
                    School.CALENDAR
                ) != null
            ) {
                (childFragmentManager.findFragmentByTag(School.CALENDAR) as CalendarFragment).getSelectedDate()
            } else {
                null
            }

        addBottomSheetDialogFragment = AddBottomSheetDialogFragment(
            this,
            this,
            lastSelectedAddCategory,
            selectedCalendarDate
        )
        addBottomSheetDialogFragment?.show(
            childFragmentManager,
            null
        )
    }

    /**
     * Sets [FragmentOverviewBinding.buttonCalendarView] image resource depending
     * on [isMonthView]
     */
    private fun setButtonCalendarViewBackground() {
        if (isMonthView) {
            binding.buttonCalendarView.setImageResource(R.drawable.ic_week_view)
        } else {
            binding.buttonCalendarView.setImageResource(R.drawable.ic_month_view)
        }
    }

    /**
     * Create fragment with tag [tag] if none exists,
     * else just show fragment with tag [tag]
     */
    private fun setFragment(tag: String) {
        when (tag) {
            School.HOME -> {
                if (childFragmentManager.findFragmentByTag(School.HOME) != null) {
                    showFragment(School.HOME)
                } else {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            HomeFragment(), School.HOME
                        ).commit()
                }
                if (childFragmentManager.findFragmentByTag(School.CALENDAR) != null) {
                    hideFragment(School.CALENDAR)
                }
                if (childFragmentManager.findFragmentByTag(School.AGENDA) != null) {
                    hideFragment(School.AGENDA)
                }

                lastSelectedAddCategory =
                    if (childFragmentManager.findFragmentByTag(School.HOME) != null) {
                        (childFragmentManager.findFragmentByTag(School.HOME) as HomeFragment).getSelectedTabPosition()
                    } else {
                        School.HOMEWORK
                    }

                binding.buttonCalendarView.visibility = View.GONE

                setLastSelectedFragment(School.HOME_SELECTED)
            }
            School.CALENDAR -> {
                if (childFragmentManager.findFragmentByTag(School.CALENDAR) != null) {
                    showFragment(School.CALENDAR)
                    binding.textViewAppBarTitle.text =
                        (childFragmentManager.findFragmentByTag(School.CALENDAR) as CalendarFragment).getSelectedMonth()
                } else {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            CalendarFragment(), School.CALENDAR
                        ).commit()
                }
                if (childFragmentManager.findFragmentByTag(School.HOME) != null) {
                    hideFragment(School.HOME)
                }
                if (childFragmentManager.findFragmentByTag(School.AGENDA) != null) {
                    hideFragment(School.AGENDA)
                }
                binding.buttonCalendarView.visibility = View.VISIBLE

                setLastSelectedFragment(School.CALENDAR_SELECTED)
            }
            School.AGENDA -> {
                if (childFragmentManager.findFragmentByTag(School.AGENDA) != null) {
                    showFragment(School.AGENDA)
                } else {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            AgendaFragment(), School.AGENDA
                        ).commit()
                }
                if (childFragmentManager.findFragmentByTag(School.HOME) != null) {
                    hideFragment(School.HOME)
                }
                if (childFragmentManager.findFragmentByTag(School.CALENDAR) != null) {
                    hideFragment(School.CALENDAR)
                }
                binding.buttonCalendarView.visibility = View.GONE

                setLastSelectedFragment(School.AGENDA_SELECTED)
            }
        }
    }

    /** Hides fragment with tag [tag] */
    private fun hideFragment(tag: String) {
        childFragmentManager.beginTransaction()
            .hide(childFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    /** Shows fragment with tag [tag] */
    private fun showFragment(tag: String) {
        childFragmentManager.beginTransaction()
            .show(childFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    /**
     * Saves last selected fragment on [sharedPref] and on
     * variable [selectedFragment]
     *
     * [fragment] One of [School.HOME_SELECTED], [School.CALENDAR_SELECTED],
     * or [School.AGENDA_SELECTED]
     */
    private fun setLastSelectedFragment(fragment: Int) {
        if (this::sharedPref.isInitialized) {
            with(sharedPref.edit()) {
                putInt(School.SELECTED_BOTTOM_NAVIGATION_FRAGMENT, fragment)
                commit()
            }
        }
        selectedFragment = fragment
    }

    /**
     * Shows [EditFragment] if [canSelectItem]
     *
     * Used when going to [EditFragment] to edit
     * an existing item from when an item is clicked
     */
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
        if (canSelectItem) {
            canSelectItem = false
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

    /**
     * Shows [EditFragment] if [canSelectItem]
     *
     * Used when going from [AddBottomSheetDialogFragment]
     * to [EditFragment]
     */
    private fun showEditFragment(
        category: Int,
        title: String,
        chipGroupDateSelected: Int,
        date: Calendar?
    ) {
        if (canSelectItem) {
            canSelectItem = false
            val editFragment = EditFragment.newInstance(
                category = category,
                title = title,
                chipGroupSelected = chipGroupDateSelected,
                selectedDate = date
            )
            editFragment.show(childFragmentManager, null)
        }
    }

    override fun goToEditFragment(
        category: Int,
        title: String,
        chipGroupDateSelected: Int,
        date: Calendar?
    ) {
        showEditFragment(
            category = category,
            title = title,
            chipGroupDateSelected = chipGroupDateSelected,
            date = date
        )
    }

    override fun selectedCategoryChanged(category: Int) {
        lastSelectedAddCategory = category
    }

    override fun dismissBottomSheet() {
        addBottomSheetDialogFragment?.dismiss()
        Timer(false).schedule(
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        ) {
            canSelectItem = true
        }
    }

    override fun itemClicked(item: Item) {
        clickCounterListener.incrementCounter {
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
                Gson().fromJson(item.subtasks, object : TypeToken<ArrayList<Subtask?>?>() {}.type),
                item.notes,
                calendar,
                item.id
            )
        }
    }

    override fun changeTitle(title: String) {
        binding.textViewAppBarTitle.text = title
    }

    override fun selectedTabChanged(category: Int) {
        lastSelectedAddCategory = category
    }

    interface OpenDrawerListener {
        fun openDrawer()
    }

    interface ClickCounterListener {
        fun incrementCounter(callback: () -> Unit)
    }

}
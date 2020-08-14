package com.dan.school.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.ItemClickListener
import com.dan.school.MainActivity
import com.dan.school.R
import com.dan.school.School
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_overview.*
import java.text.SimpleDateFormat
import java.util.*

class OverviewFragment : Fragment(),
    AddBottomSheetDialogFragment.GoToEditFragment,
    AddBottomSheetDialogFragment.SelectedCategoryChangeListener,
    EditFragment.CategoryChangeListener,
    EditFragment.DismissBottomSheetListener,
    ItemClickListener, CalendarFragment.TitleChangeListener,
    HomeFragment.SelectedTabChangeListener {

    private var addBottomSheetDialogFragment: AddBottomSheetDialogFragment? = null
    private var lastSelectedAddCategory = School.HOMEWORK
    private var isMonthView = true

    private var selectedFragment = 0

    private lateinit var openDrawerListener: OpenDrawerListener

    private lateinit var sharedPref: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        if (activity is MainActivity) {
            openDrawerListener = activity as MainActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_overview, container, false)
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
                    bottomNavigation.selectedItemId =
                        R.id.homeFragment
                }
                School.CALENDAR_SELECTED -> {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            CalendarFragment(), School.CALENDAR
                        ).commit()
                    buttonCalendarView.visibility = View.VISIBLE
                    bottomNavigation.selectedItemId =
                        R.id.calendarFragment
                }
                School.AGENDA_SELECTED -> {
                    childFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            AgendaFragment(), School.AGENDA
                        ).commit()
                    bottomNavigation.selectedItemId =
                        R.id.agendaFragment
                }
            }
        }

        setButtonCalendarViewBackground()
        if (selectedFragment == School.CALENDAR_SELECTED) {
            buttonCalendarView.visibility = View.VISIBLE
        }

        // Listeners
        floatingActionButton.setOnClickListener {
            addBottomSheetDialogFragment =
                AddBottomSheetDialogFragment(
                    this,
                    this,
                    lastSelectedAddCategory
                )
            addBottomSheetDialogFragment?.show(
                childFragmentManager,
                "BottomSheet"
            )
        }
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    setFragment(School.HOME)
                    textViewAppBarTitle.text = getString(R.string.app_name)
                }
                R.id.calendarFragment -> {
                    setFragment(School.CALENDAR)
                }
                R.id.agendaFragment -> {
                    setFragment(School.AGENDA)
                    textViewAppBarTitle.text = getString(R.string.app_name)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
        buttonMenu.setOnClickListener {
            if (this::openDrawerListener.isInitialized) {
                openDrawerListener.openDrawer()
            }
        }
        buttonCalendarView.setOnClickListener {
            if (childFragmentManager.findFragmentByTag(School.CALENDAR) != null) {
                (childFragmentManager.findFragmentByTag(School.CALENDAR) as CalendarFragment).setCalendarView(
                    isMonthView
                )
                isMonthView = !isMonthView
                setButtonCalendarViewBackground()
            }
        }
    }

    private fun setButtonCalendarViewBackground() {
        if (isMonthView) {
            buttonCalendarView.setImageResource(R.drawable.ic_week_view)
        } else {
            buttonCalendarView.setImageResource(R.drawable.ic_month_view)
        }
    }

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

                buttonCalendarView.visibility = View.GONE

                setLastSelectedFragment(School.HOME_SELECTED)
            }
            School.CALENDAR -> {
                if (childFragmentManager.findFragmentByTag(School.CALENDAR) != null) {
                    showFragment(School.CALENDAR)
                    textViewAppBarTitle.text =
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
                buttonCalendarView.visibility = View.VISIBLE

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
                buttonCalendarView.visibility = View.GONE

                setLastSelectedFragment(School.AGENDA_SELECTED)
            }
        }
    }

    private fun hideFragment(tag: String) {
        childFragmentManager.beginTransaction()
            .hide(childFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    private fun showFragment(tag: String) {
        childFragmentManager.beginTransaction()
            .show(childFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    private fun setLastSelectedFragment(fragment: Int) {
        if (this::sharedPref.isInitialized) {
            with(sharedPref.edit()) {
                putInt(School.SELECTED_BOTTOM_NAVIGATION_FRAGMENT, fragment)
                commit()
            }
        }
    }

    private fun showEditFragment(
        category: Int,
        done: Boolean,
        title: String,
        subtasks: ArrayList<Subtask>,
        notes: String,
        date: Calendar?,
        itemId: Int
    ) {
        val editFragment = EditFragment.newInstance(
            category = category,
            done = done,
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

    private fun showEditFragment(
        category: Int,
        title: String,
        chipGroupDateSelected: Int,
        date: Calendar?
    ) {
        val editFragment = EditFragment.newInstance(
            category = category,
            title = title,
            chipGroupSelected = chipGroupDateSelected,
            selectedDate = date
        )
        editFragment.show(childFragmentManager, "editFragment")
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
            item.title,
            Gson().fromJson(item.subtasks, object : TypeToken<ArrayList<Subtask?>?>() {}.type),
            item.notes,
            calendar,
            item.id
        )
    }

    override fun changeTitle(title: String) {
        textViewAppBarTitle.text = title
    }

    override fun selectedTabChanged(category: Int) {
        lastSelectedAddCategory = category
    }

    interface OpenDrawerListener {
        fun openDrawer()
    }

}
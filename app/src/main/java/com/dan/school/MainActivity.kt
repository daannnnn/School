package com.dan.school

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dan.school.School.AGENDA
import com.dan.school.School.CALENDAR
import com.dan.school.School.HOME
import com.dan.school.fragments.*
import com.dan.school.models.Item
import com.dan.school.models.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),
    AddBottomSheetDialogFragment.GoToEditFragment,
    EditFragment.DismissBottomSheetListener,
    AddBottomSheetDialogFragment.SelectedCategoryChangeListener,
    HomeFragment.SelectedTabChangeListener, EditFragment.CategoryChangeListener,
    ItemClickListener, CalendarFragment.TitleChangeListener,
    DialogInterface.OnDismissListener {

    private var addBottomSheetDialogFragment: AddBottomSheetDialogFragment? = null
    private var lastSelectedAddCategory = School.HOMEWORK
    private var isMonthView = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigationView.menu.getItem(0).isChecked = true
            setButtonCalendarViewBackground()

            // Show HomeFragment
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutBottomNavigation,
                    HomeFragment(), HOME
                ).commit()
        }

        if (isMonthView) {
            buttonCalendarView.setImageResource(R.drawable.ic_week_view)
        } else {
            buttonCalendarView.setImageResource(R.drawable.ic_month_view)
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
                supportFragmentManager,
                "BottomSheet"
            )
        }
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    setFragment(HOME)
                    textViewAppBarTitle.text = getString(R.string.app_name)
                }
                R.id.calendarFragment -> {
                    setFragment(CALENDAR)
                }
                R.id.agendaFragment -> {
                    setFragment(AGENDA)
                    textViewAppBarTitle.text = getString(R.string.app_name)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
        buttonMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        buttonCalendarView.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag(CALENDAR) != null) {
                (supportFragmentManager.findFragmentByTag(CALENDAR) as CalendarFragment).setCalendarView(
                    isMonthView
                )
                isMonthView = !isMonthView
                setButtonCalendarViewBackground()
            }
        }
        navigationView.setNavigationItemSelectedListener { item ->
            if (!item.isChecked) {
                item.isChecked = true
                drawerLayout.closeDrawers()

                when (item.itemId) {
                    R.id.settings -> {
                        val settingsFragment =
                            SettingsFragment()
                        settingsFragment.show(supportFragmentManager, "settingsFragment")
                    }
                }
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            return@setNavigationItemSelectedListener true
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
            HOME -> {
                if (supportFragmentManager.findFragmentByTag(HOME) != null) {
                    showFragment(HOME)
                } else {
                    supportFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            HomeFragment(), HOME
                        ).commit()
                }
                if (supportFragmentManager.findFragmentByTag(CALENDAR) != null) {
                    hideFragment(CALENDAR)
                }
                if (supportFragmentManager.findFragmentByTag(AGENDA) != null) {
                    hideFragment(AGENDA)
                }
                lastSelectedAddCategory =
                    (supportFragmentManager.findFragmentByTag(HOME) as HomeFragment).getSelectedTabPosition()
                buttonCalendarView.visibility = View.GONE
            }
            CALENDAR -> {
                if (supportFragmentManager.findFragmentByTag(CALENDAR) != null) {
                    showFragment(CALENDAR)
                    textViewAppBarTitle.text =
                        (supportFragmentManager.findFragmentByTag(CALENDAR) as CalendarFragment).getSelectedMonth()
                } else {
                    supportFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            CalendarFragment(), CALENDAR
                        ).commit()
                }
                if (supportFragmentManager.findFragmentByTag(HOME) != null) {
                    hideFragment(HOME)
                }
                if (supportFragmentManager.findFragmentByTag(AGENDA) != null) {
                    hideFragment(AGENDA)
                }
                buttonCalendarView.visibility = View.VISIBLE
            }
            AGENDA -> {
                if (supportFragmentManager.findFragmentByTag(AGENDA) != null) {
                    showFragment(AGENDA)
                } else {
                    supportFragmentManager.beginTransaction()
                        .add(
                            R.id.frameLayoutBottomNavigation,
                            AgendaFragment(), AGENDA
                        ).commit()
                }
                if (supportFragmentManager.findFragmentByTag(HOME) != null) {
                    hideFragment(HOME)
                }
                if (supportFragmentManager.findFragmentByTag(CALENDAR) != null) {
                    hideFragment(CALENDAR)
                }
                buttonCalendarView.visibility = View.GONE
            }
        }
    }

    private fun hideFragment(tag: String) {
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    private fun showFragment(tag: String) {
        supportFragmentManager.beginTransaction()
            .show(supportFragmentManager.findFragmentByTag(tag)!!).commit()
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
        editFragment.show(supportFragmentManager, "editFragment")
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
        editFragment.show(supportFragmentManager, "editFragment")
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

    override fun dismissBottomSheet() {
        addBottomSheetDialogFragment?.dismiss()
    }

    override fun selectedCategoryChanged(category: Int) {
        lastSelectedAddCategory = category
    }

    override fun selectedTabChanged(category: Int) {
        lastSelectedAddCategory = category
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

    override fun onDismiss(dialog: DialogInterface?) {
        navigationView.setCheckedItem(R.id.overview)
    }
}
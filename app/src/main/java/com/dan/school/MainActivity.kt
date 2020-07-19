package com.dan.school

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dan.school.fragments.*
import com.dan.school.models.Item
import com.dan.school.models.Reminder
import com.dan.school.models.Subtask
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),
    AddBottomSheetDialogFragment.GoToEditFragment,
    EditFragment.DismissBottomSheetListener,
    SettingsFragment.OnDismissListener,
    AddBottomSheetDialogFragment.SelectedCategoryChangeListener,
    HomeFragment.SelectedTabChangeListener, EditFragment.CategoryChangeListener,
    HomeFragment.ItemClickListener, CalendarFragment.TitleChangeListener {

    private var addBottomSheetDialogFragment: AddBottomSheetDialogFragment? = null
    private var lastSelectedAddCategory = School.HOMEWORK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView.menu.getItem(0).isChecked = true

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
                    setFragment("home")
                }
                R.id.calendarFragment -> {
                    setFragment("calendar")
                }
                R.id.agendaFragment -> {
                    setFragment("agenda")
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
        buttonMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navigationView.setNavigationItemSelectedListener { item ->
            if (!item.isChecked) {
                item.isChecked = true
                drawerLayout.closeDrawers()

                when (item.itemId) {
                    R.id.settings -> {
                        val settingsFragment =
                            SettingsFragment(this)
                        settingsFragment.show(supportFragmentManager, "settingsFragment")
                    }
                }
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            return@setNavigationItemSelectedListener true
        }

//        // Show HomeFragment
//        supportFragmentManager.beginTransaction()
//            .add(
//                R.id.frameLayoutBottomNavigation,
//                HomeFragment(this, this), "home"
//            ).commit()
        supportFragmentManager.beginTransaction()
            .add(
                R.id.frameLayoutBottomNavigation,
                CalendarFragment(this), "calendar"
            ).commit()
    }

    private fun setFragment(tag: String) {
        if (tag == "home") {
            if (supportFragmentManager.findFragmentByTag("home") != null) {
                supportFragmentManager.beginTransaction()
                    .show(supportFragmentManager.findFragmentByTag("home")!!).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.frameLayoutBottomNavigation,
                        HomeFragment(this, this), "home"
                    ).commit()
            }
            if (supportFragmentManager.findFragmentByTag("calendar") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
            if (supportFragmentManager.findFragmentByTag("agenda") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
//            lastSelectedAddCategory =
//                (supportFragmentManager.findFragmentByTag("home") as HomeFragment).getSelectedTabPosition()
        } else if (tag == "calendar") {
            if (supportFragmentManager.findFragmentByTag("calendar") != null) {
                supportFragmentManager.beginTransaction()
                    .show(supportFragmentManager.findFragmentByTag("calendar")!!).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.frameLayoutBottomNavigation,
                        CalendarFragment(this), "calendar"
                    ).commit()
            }
            if (supportFragmentManager.findFragmentByTag("home") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if (supportFragmentManager.findFragmentByTag("agenda") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("agenda")!!).commit()
            }
        } else if (tag == "agenda") {
            if (supportFragmentManager.findFragmentByTag("agenda") != null) {
                supportFragmentManager.beginTransaction()
                    .show(supportFragmentManager.findFragmentByTag("agenda")!!).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.frameLayoutBottomNavigation,
                        AgendaFragment(), "agenda"
                    ).commit()
            }
            if (supportFragmentManager.findFragmentByTag("home") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("home")!!).commit()
            }
            if (supportFragmentManager.findFragmentByTag("calendar") != null) {
                supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag("calendar")!!).commit()
            }
        }
    }

    private fun showEditFragment(
        category: Int,
        title: String,
        subtasks: ArrayList<Subtask>,
        reminders: ArrayList<Reminder>,
        notes: String,
        date: Calendar?,
        itemId: Int
    ) {
        val editFragment = EditFragment(
            categoryChangeListener = this,
            dismissBottomSheetListener = this,
            category = category,
            title = title,
            subtasks = subtasks,
            reminders = reminders,
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
        val editFragment = EditFragment(
            categoryChangeListener = this,
            dismissBottomSheetListener = this,
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

    override fun onDismiss() {
        navigationView.setCheckedItem(R.id.overview)
    }

    override fun selectedCategoryChanged(category: Int) {
        lastSelectedAddCategory = category
    }

    override fun selectedTabChanged(category: Int) {
        lastSelectedAddCategory = category
    }

    override fun itemClicked(item: Item) {
        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat(School.dateFormat, Locale.getDefault()).parse(item.date)!!
        showEditFragment(
            item.category,
            item.title,
            item.subtasks,
            item.reminders,
            item.notes,
            calendar,
            item.id
        )
    }

    override fun changeTitle(title: String) {
        textViewAppBarTitle.text = title
    }
}
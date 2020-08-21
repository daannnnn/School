package com.dan.school.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dan.school.*
import com.dan.school.adapters.HomeworkExamTaskTabLayoutAdapter
import com.dan.school.adapters.ItemListAdapter
import com.dan.school.models.Item
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_overview.*

class HomeFragment : Fragment(),
    ItemClickListener {

    val categoryColors = ArrayList<Int>()

    private lateinit var selectedTabChangeListener: SelectedTabChangeListener
    private lateinit var itemClickListener: ItemClickListener

    private lateinit var sharedPref: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OverviewFragment) {
            selectedTabChangeListener = (parentFragment as OverviewFragment)
            itemClickListener = (parentFragment as OverviewFragment)
        }
        sharedPref = context.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // colors for each category
        categoryColors.add(
            ContextCompat.getColor(
                requireContext(),
                R.color.homeworkColor
            )
        )
        categoryColors.add(
            ContextCompat.getColor(
                requireContext(),
                R.color.examColor
            )
        )
        categoryColors.add(
            ContextCompat.getColor(
                requireContext(),
                R.color.taskColor
            )
        )

        // [START configure TabLayout and ViewPager]
        val homeworksFragment = ItemsFragment.newInstance(School.HOMEWORK)
        val examsFragment = ItemsFragment.newInstance(School.EXAM)
        val tasksFragment = ItemsFragment.newInstance(School.TASK)

        val mListFragments = ArrayList<Fragment>()
        mListFragments.add(homeworksFragment)
        mListFragments.add(examsFragment)
        mListFragments.add(tasksFragment)

        val mListFragmentTitles = ArrayList<String>()  // fragment titles
        mListFragmentTitles.add(getString(R.string.homeworks))
        mListFragmentTitles.add(getString(R.string.exams))
        mListFragmentTitles.add(getString(R.string.tasks))

        val adapter = HomeworkExamTaskTabLayoutAdapter(
            mListFragments,
            mListFragmentTitles,
            childFragmentManager
        )

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // change color when selecting tab
                val position = tab!!.position
                if (this@HomeFragment::selectedTabChangeListener.isInitialized) {
                    selectedTabChangeListener.selectedTabChanged(position)
                }
                tabLayout.setSelectedTabIndicatorColor(categoryColors[position])
                tabLayout.setTabTextColors(
                    tabLayout.tabTextColors!!.defaultColor,
                    categoryColors[position]
                )
                setLastSelectedTab(position)
            }
        })
        // [END configure TabLayout and ViewPager]

        tabLayout.selectTab(
            tabLayout.getTabAt(
                sharedPref.getInt(
                    School.SELECTED_TAB_FRAGMENT,
                    School.HOMEWORK
                )
            )
        )
    }

    /**
     * Saves last selected tab position on [sharedPref]
     *
     * [tabPosition] One of [School.HOMEWORK], [School.EXAM],
     * or [School.TASK]
     */
    private fun setLastSelectedTab(tabPosition: Int) {
        if (this::sharedPref.isInitialized) {
            with(sharedPref.edit()) {
                putInt(School.SELECTED_TAB_FRAGMENT, tabPosition)
                commit()
            }
        }
    }

    fun getSelectedTabPosition(): Int {
        return tabLayout.selectedTabPosition
    }

    override fun itemClicked(item: Item) {
        if (this::itemClickListener.isInitialized) {
            itemClickListener.itemClicked(item)
        }
    }

    /**
     * Callback to be invoked every time [tabLayout]
     * selected tab is changed
     */
    interface SelectedTabChangeListener {
        fun selectedTabChanged(category: Int)
    }
}
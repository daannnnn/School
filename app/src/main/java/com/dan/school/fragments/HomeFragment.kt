package com.dan.school.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dan.school.adapters.HomeworkExamTaskTabLayoutAdapter
import com.dan.school.R
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment(var selectedTabChangeListener: SelectedTabChangeListener) : Fragment() {

    val categoryColors = ArrayList<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // colors for each category
        categoryColors.add(ContextCompat.getColor(requireContext(),
            R.color.homeworkColor
        ))
        categoryColors.add(ContextCompat.getColor(requireContext(),
            R.color.examColor
        ))
        categoryColors.add(ContextCompat.getColor(requireContext(),
            R.color.taskColor
        ))

        // [START configure TabLayout and ViewPager]
        val homeworksFragment = HomeworksFragment()
        val examsFragment = ExamsFragment()
        val tasksFragment = TasksFragment()

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
                selectedTabChangeListener.selectedTabChanged(position)
                tabLayout.setSelectedTabIndicatorColor(categoryColors[position])
                tabLayout.setTabTextColors(tabLayout.tabTextColors!!.defaultColor, categoryColors[position])
            }
        })
        // [END configure TabLayout and ViewPager]
    }

    fun getSelectedTabPosition(): Int {
        return tabLayout.selectedTabPosition
    }

    interface SelectedTabChangeListener {
        fun selectedTabChanged(category: Int)
    }
}
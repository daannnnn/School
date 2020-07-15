package com.dan.school.adapters

import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class HomeworkExamTaskTabLayoutAdapter(private val mListFragments: ArrayList<Fragment>, private val mListFragmentTitles: ArrayList<String>, fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return mListFragments[position]
    }

    override fun getCount(): Int {
        return mListFragments.size
    }

    @Nullable
    override fun getPageTitle(position: Int): CharSequence? {
        return mListFragmentTitles[position]
    }

}
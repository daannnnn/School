package com.dan.school.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dan.school.ui.fragments.setup.SetupIllustrationFragment

class SetupViewPagerAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return SetupIllustrationFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return 3
    }

}
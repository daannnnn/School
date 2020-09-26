package com.dan.school.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.dan.school.R
import com.dan.school.SetupActivity
import com.dan.school.adapters.SetupViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_setup_view_pager.*

class SetupViewPagerFragment : Fragment() {

    private var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager.adapter = SetupViewPagerAdapter(childFragmentManager)
        setupTabLayout.setupWithViewPager(setupViewPager)

        if (currentPosition == 2) {
            buttonDoneSkip.text = getString(R.string.done)
        } else {
            buttonDoneSkip.text = getString(R.string.skip)
        }

        setupViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
            override fun onPageSelected(position: Int) {
                if (position == 2) {
                    buttonDoneSkip.text = getString(R.string.done)
                } else {
                    buttonDoneSkip.text = getString(R.string.skip)
                }
                currentPosition = position
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })

        buttonDoneSkip.setOnClickListener {
            if (currentPosition == 2) {
                if (activity is SetupActivity) {
                    (activity as SetupActivity).setupDone()
                }
            } else {
                setupViewPager.setCurrentItem(2, true)
            }
        }
    }

}
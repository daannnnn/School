package com.dan.school.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.dan.school.R
import com.dan.school.adapters.SetupViewPagerAdapter
import com.dan.school.databinding.FragmentSetupViewPagerBinding

class SetupViewPagerFragment : Fragment() {

    private var _binding: FragmentSetupViewPagerBinding? = null

    private val binding get() = _binding!!

    private var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupViewPager.adapter = SetupViewPagerAdapter(childFragmentManager)
        binding.setupTabLayout.setupWithViewPager(binding.setupViewPager)

        if (currentPosition == 2) {
            binding.buttonDoneSkip.text = getString(R.string.done)
        } else {
            binding.buttonDoneSkip.text = getString(R.string.skip)
        }

        binding.setupViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
            override fun onPageSelected(position: Int) {
                if (position == 2) {
                    binding.buttonDoneSkip.text = getString(R.string.done)
                } else {
                    binding.buttonDoneSkip.text = getString(R.string.skip)
                }
                currentPosition = position
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })

        binding.buttonDoneSkip.setOnClickListener {
            if (currentPosition == 2) {
                if (activity is SetupActivity) {
                    (activity as SetupActivity).setupDone()
                }
            } else {
                binding.setupViewPager.setCurrentItem(2, true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
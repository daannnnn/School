package com.dan.school.ui.fragments.completed

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dan.school.other.DataViewModel
import com.dan.school.ui.activities.MainActivity
import com.dan.school.R
import com.dan.school.other.School
import com.dan.school.databinding.FragmentCompletedBinding
import com.dan.school.ui.fragments.overview.OverviewFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest

const val ROTATION = "rotation"

class CompletedFragment : Fragment() {

    private var _binding: FragmentCompletedBinding? = null

    private val binding get() = _binding!!

    private var isOptionsExpanded = false
    private lateinit var displaySortByStringArray: Array<String>
    private var selectedIndex = 0

    private val dataViewModel: DataViewModel by activityViewModels()

    private val sortByArray = arrayOf(School.DONE_TIME, School.TITLE)

    private lateinit var openDrawerListener: OverviewFragment.OpenDrawerListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        displaySortByStringArray = resources.getStringArray(R.array.sort_by_array)
        if (activity is MainActivity) {
            openDrawerListener = activity as MainActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adRequest = AdRequest.Builder().build()
        binding.adViewBannerCompletedFragment.adListener = object: AdListener() {
            override fun onAdLoaded() {
                binding.adViewBannerCompletedFragment.visibility = View.VISIBLE
            }
        }
        binding.adViewBannerCompletedFragment.loadAd(adRequest)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutCompleted,
                    CompletedNotGroupedFragment()
                ).commit()
        }

        dataViewModel.getDoneItems().observe(viewLifecycleOwner, { overdueItems ->
            if (overdueItems.isEmpty()) {
                binding.linearLayoutCompletedItems.isVisible = false
                binding.linearLayoutNoCompletedItems.isVisible = true
            } else {
                binding.linearLayoutCompletedItems.isVisible = true
                binding.linearLayoutNoCompletedItems.isVisible = false
            }
        })

        binding.relativeLayoutOptionsFragment.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.linearLayoutCompletedItems.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        binding.textSwitcherSortBy.setFactory {
            val textView = TextView(requireContext())
            textView.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                typeface = ResourcesCompat.getFont(requireContext(), R.font.cabin_bold)
            }
            textView
        }
        binding.textSwitcherSortBy.setText(displaySortByStringArray[selectedIndex])

        binding.textSwitcherSortBy.setOnClickListener {
            if (selectedIndex > 0) {
                selectedIndex = 0
            } else {
                selectedIndex++
            }
            binding.textSwitcherSortBy.setText(displaySortByStringArray[selectedIndex])
            dataViewModel.setSortBy(sortByArray[selectedIndex])
        }

        binding.buttonOptions.setOnClickListener {
            isOptionsExpanded = if (isOptionsExpanded) {
                ObjectAnimator.ofFloat(binding.imageViewOptions, ROTATION, 0f)
                    .setDuration(250)
                    .start()
                binding.linearLayoutOptions.visibility = View.GONE
                false
            } else {
                ObjectAnimator.ofFloat(binding.imageViewOptions, ROTATION, 180f)
                    .setDuration(250)
                    .start()
                binding.linearLayoutOptions.visibility = View.VISIBLE
                true
            }
        }
        binding.switchGroupByCategory.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.frameLayoutCompleted,
                        CompletedGroupedFragment()
                    ).commit()
            } else {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.frameLayoutCompleted,
                        CompletedNotGroupedFragment()
                    ).commit()
            }
        }
        binding.buttonMenu.setOnClickListener {
            if (this::openDrawerListener.isInitialized) {
                openDrawerListener.openDrawer()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
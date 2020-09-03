package com.dan.school.fragments

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dan.school.DataViewModel
import com.dan.school.MainActivity
import com.dan.school.R
import com.dan.school.School
import kotlinx.android.synthetic.main.fragment_completed.*

class CompletedFragment : Fragment() {

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
    ): View? {
        return inflater.inflate(R.layout.fragment_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutCompleted,
                    CompletedNotGroupedFragment()
                ).commit()
        }

        dataViewModel.getDoneItems().observe(viewLifecycleOwner, androidx.lifecycle.Observer { overdueItems ->
            if (overdueItems.isEmpty()) {
                linearLayoutCompletedItems.isVisible = false
                linearLayoutNoCompletedItems.isVisible = true
            } else {
                linearLayoutCompletedItems.isVisible = true
                linearLayoutNoCompletedItems.isVisible = false
            }
        })

        relativeLayoutOptionsFragment.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        linearLayoutCompletedItems.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        textSwitcherSortBy.setFactory {
            val textView = TextView(requireContext())
            textView.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                typeface = ResourcesCompat.getFont(requireContext(), R.font.cabin_bold)
            }
            textView
        }
        textSwitcherSortBy.setText(displaySortByStringArray[selectedIndex])

        textSwitcherSortBy.setOnClickListener {
            if (selectedIndex > 0) {
                selectedIndex = 0
            } else {
                selectedIndex++
            }
            textSwitcherSortBy.setText(displaySortByStringArray[selectedIndex])
            dataViewModel.setSortBy(sortByArray[selectedIndex])
        }
        buttonOptions.setOnClickListener {
            isOptionsExpanded = if (isOptionsExpanded) {
                ObjectAnimator.ofFloat(imageViewOptions, "rotation", 0f)
                    .setDuration(250)
                    .start()
                linearLayoutOptions.visibility = View.GONE
                false
            } else {
                ObjectAnimator.ofFloat(imageViewOptions, "rotation", 180f)
                    .setDuration(250)
                    .start()
                linearLayoutOptions.visibility = View.VISIBLE
                true
            }
        }
        switchGroupByCategory.setOnCheckedChangeListener { _, isChecked ->
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
        buttonMenu.setOnClickListener {
            if (this::openDrawerListener.isInitialized) {
                openDrawerListener.openDrawer()
            }
        }
    }
}
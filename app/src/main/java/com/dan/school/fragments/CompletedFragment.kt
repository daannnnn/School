package com.dan.school.fragments

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
import androidx.fragment.app.DialogFragment
import com.dan.school.R
import kotlinx.android.synthetic.main.fragment_completed.*

class CompletedFragment : DialogFragment() {

    private var isOptionsExpanded = false
    private lateinit var sortByArray: Array<String>
    private var selectedIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sortByArray = resources.getStringArray(R.array.sort_by_array)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        linearLayoutOptionsFragment.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
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
        textSwitcherSortBy.setText(sortByArray[selectedIndex])

        textSwitcherSortBy.setOnClickListener {
            if (selectedIndex > 0) {
                selectedIndex = 0
            } else {
                selectedIndex++
            }
            textSwitcherSortBy.setText(sortByArray[selectedIndex])
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
    }
}
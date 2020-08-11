package com.dan.school.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.dan.school.R
import kotlinx.android.synthetic.main.fragment_completed.*

class CompletedFragment : DialogFragment() {

    private var isOptionsExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_by_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSortBy.adapter = adapter
        }
    }

}
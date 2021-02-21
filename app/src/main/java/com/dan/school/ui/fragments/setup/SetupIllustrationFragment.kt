package com.dan.school.ui.fragments.setup

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.dan.school.other.School.POSITION
import com.dan.school.databinding.FragmentSetupIllustrationBinding

class SetupIllustrationFragment : Fragment() {

    private var _binding: FragmentSetupIllustrationBinding? = null

    private val binding get() = _binding!!

    private var position: Int = -1

    private val illustrationArray = arrayOf(
        R.drawable.school_setup_illustration_1,
        R.drawable.school_setup_illustration_2,
        R.drawable.school_setup_illustration_3
    )

    private val stringArray = arrayOf(
        R.string.add_homeworks_exams_and_tasks,
        R.string.check_your_calendar,
        R.string.see_your_schedule_for_the_day
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(POSITION, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupIllustrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (position != -1) {
            binding.imageViewIllustration.setImageResource(illustrationArray[position])
            if (position == 0) {
                val spannable = SpannableString(getString(stringArray[position]))

                spannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.homeworkColor
                        )
                    ), 4, 14, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.examColor
                        )
                    ), 15, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )

                spannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.examColor
                        )
                    ), 26, 31, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )

                binding.textViewMessage.setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                binding.textViewMessage.text = getString(stringArray[position])
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(position: Int) =
            SetupIllustrationFragment().apply {
                arguments = Bundle().apply {
                    putInt(POSITION, position)
                }
            }
    }
}
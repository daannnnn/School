package com.dan.school

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import kotlinx.android.synthetic.main.fragment_edit.*

class EditFragment(private val listener: DismissBottomSheet, private val category: Int) : DialogFragment() {

    private val colors = arrayOf(R.color.homeworkColor, R.color.examColor, R.color.taskColor)
    private var currentPosition = School.Category.HOMEWORK

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialog
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener.dismissBottomSheet()

        val viewPagerAdapter = InfinitePagerAdapter(CategoryViewPagerAdapter(requireContext()))

        viewPagerCategory.adapter = viewPagerAdapter
        viewPagerCategory.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val realPosition = viewPagerCategory.currentItem

                val colorFrom = ContextCompat.getColor(requireContext(), colors[currentPosition])
                val colorTo = ContextCompat.getColor(requireContext(), colors[realPosition])
                val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                colorAnimation.addUpdateListener { animator ->
                    textViewDatePicked.setTextColor(
                        animator.animatedValue as Int
                    )
                }
                colorAnimation.start()

                currentPosition = realPosition
            }
        })

        // listeners
        buttonShowMore.setOnClickListener {
            constraintLayoutMore.visibility = View.VISIBLE
            buttonShowMore.visibility = View.GONE
        }

        // initialize
        currentPosition = category
        viewPagerCategory.currentItem += currentPosition
        textViewDatePicked.setTextColor(ContextCompat.getColor(requireContext(), colors[currentPosition]))
    }

    interface DismissBottomSheet {
        fun dismissBottomSheet()
    }
}
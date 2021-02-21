package com.dan.school.adapters

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.PagerAdapter
import com.dan.school.R
import com.dan.school.other.School

class CategoryViewPagerAdapter(val context: Context) :
    PagerAdapter() {

    private val titles = arrayOf(School.HOMEWORK_TITLE, School.EXAM_TITLE, School.TASK_TITLE)
    private val colors = arrayOf(
        R.color.homeworkColor,
        R.color.examColor,
        R.color.taskColor
    )

    override fun getCount(): Int {
        return titles.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = TextView(context)
        v.text = titles[position]
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        v.setTextColor(ContextCompat.getColor(context, colors[position]))
        v.typeface = ResourcesCompat.getFont(
            context,
            R.font.cabin_bold
        )
        v.gravity = Gravity.CENTER
        container.addView(v, 0)
        return v
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}
package com.dan.school.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.dan.school.R

class SettingsItem : RelativeLayout {

    private var icon: Drawable? = null
    private var text: String? = null
    private var showSelectedTextView: Boolean? = null
    private var selectedText: String? = null

    private var imageViewIcon: ImageView? = null
    private var textViewText: TextView? = null
    private var textViewSelected: TextView? = null

    constructor(context: Context) : super(context) {
        initializeViews(context)
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(
        context,
        attrs,
        defStyle,
        0
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SettingsItem,
            0, 0
        ).apply {
            try {
                icon = getDrawable(R.styleable.SettingsItem_icon)
                text = getString(R.styleable.SettingsItem_text)
                showSelectedTextView = getBoolean(R.styleable.SettingsItem_showSelectedTextView, false)
                selectedText = getString(R.styleable.SettingsItem_selectedText)
            } finally {
                recycle()
            }
        }
        initializeViews(context)
    }

    private fun initializeViews(context: Context) {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_settings_item, this)

        imageViewIcon = findViewById(R.id.imageViewIcon)
        textViewText = findViewById(R.id.textViewText)
        textViewSelected = findViewById(R.id.textViewSelected)

        isClickable = true
        isFocusable = true

        val dp24 = (24 * resources.displayMetrics.density + 0.5f).toInt()
        setPadding(dp24, 0, dp24, 0)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)

        imageViewIcon?.setImageDrawable(icon)
        textViewText?.text = text
        textViewSelected?.let {
            it.isVisible = showSelectedTextView == true
            it.text = selectedText
        }
    }

    fun setSelectedText(text: String) {
        textViewSelected?.text = text
    }
}
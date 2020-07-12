package com.dan.school

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

class SubtaskListAdapter(
    private val mContext: Context,
    private val dataChangedListener: DataChangeListener,
    val setFocusListener: SetFocusListener,
    private val data: ArrayList<Subtask>,
    var iconChecked: Int,
    var iconUnchecked: Int
) :
    ArrayAdapter<Subtask>(mContext, R.layout.layout_subtask_item, data) {

    private var inputMethodManager: InputMethodManager =
        mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val mView: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(mContext)
            mView = inflater.inflate(R.layout.layout_subtask_item, parent, false)
            val editTextSubtaskTitle = mView.findViewById<EditText>(R.id.editTextSubtaskTitle)
            editTextSubtaskTitle.setText(getItem(position)?.title)
            editTextSubtaskTitle.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (editTextSubtaskTitle.text.toString() == "") {
                        if (position == count - 1) {
                            data.removeAt(position)
                            notifyDataSetChanged()
                            dataChangedListener.dataChanged()
                            inputMethodManager.toggleSoftInput(
                                InputMethodManager.HIDE_IMPLICIT_ONLY,
                                0
                            )
                        }
                    } else {
                        if (position == count - 1) {
                            add(Subtask())
                        } else {
                            setFocusListener.setFocus(position+1)
                        }
                    }
                }
                return@setOnEditorActionListener true
            }

            val buttonCheck = mView.findViewById<ImageButton>(R.id.buttonCheck)
            buttonCheck.setImageResource(iconUnchecked)
            buttonCheck.setOnClickListener {
                if (getItem(position)?.done!!) {
                    buttonCheck.setImageResource(iconUnchecked)
                    getItem(position)?.done = false
                } else {
                    buttonCheck.setImageResource(iconChecked)
                    getItem(position)?.done = true
                }
            }
        } else {
            mView = convertView
        }

        if (position == count - 1) {
            mView.findViewById<TextView>(R.id.editTextSubtaskTitle).requestFocus()
        }

        return mView
    }

    override fun add(subtask: Subtask?) {
        super.add(subtask)
        dataChangedListener.dataChanged()
    }

    interface DataChangeListener {
        fun dataChanged()
    }

    interface SetFocusListener {
        fun setFocus(position: Int)
    }
}
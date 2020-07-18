package com.dan.school.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.models.Subtask

class SubtaskListAdapter(
    private val context: Context,
    private val setFocusListener: SetFocusListener,
    var iconChecked: Int,
    var iconUnchecked: Int,
    val data: ArrayList<Subtask>,
    private var getFocus: Boolean
) : RecyclerView.Adapter<SubtaskListAdapter.SubtaskViewHolder>() {

    private var inputMethodManager: InputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    class SubtaskViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val editTextSubtaskTitle: EditText = view.findViewById(R.id.editTextSubtaskTitle)
        val buttonCheck: ImageButton = view.findViewById(R.id.buttonCheck)
        val buttonRemove: ImageButton = view.findViewById(R.id.buttonRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_subtask_item, parent, false)
        return SubtaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.editTextSubtaskTitle.setText(data[holder.bindingAdapterPosition].title)
        holder.editTextSubtaskTitle.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (holder.editTextSubtaskTitle.text.toString() == "") {
                    if (holder.bindingAdapterPosition == itemCount - 1) {
                        data.removeAt(holder.bindingAdapterPosition)
                        notifyItemRemoved(holder.bindingAdapterPosition)
                        inputMethodManager.toggleSoftInput(
                            InputMethodManager.HIDE_IMPLICIT_ONLY,
                            0
                        )
                    }
                } else {
                    if (holder.bindingAdapterPosition == itemCount - 1) {
                        data.add(Subtask())
                        notifyItemInserted(itemCount - 1)
                    } else {
                        setFocusListener.setFocus(holder.bindingAdapterPosition + 1)
                    }
                }
            }
            return@setOnEditorActionListener true
        }
        holder.editTextSubtaskTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                data[holder.bindingAdapterPosition].title = s.toString()
            }
        })
        holder.buttonCheck.setImageResource(iconUnchecked)
        holder.buttonCheck.setOnClickListener {
            if (data[holder.bindingAdapterPosition].done) {
                holder.buttonCheck.setImageResource(iconUnchecked)
                data[holder.bindingAdapterPosition].done = false
            } else {
                holder.buttonCheck.setImageResource(iconChecked)
                data[holder.bindingAdapterPosition].done = true
            }
        }
        holder.buttonRemove.setOnClickListener {
            if (holder.editTextSubtaskTitle.hasFocus()) {
                holder.editTextSubtaskTitle.clearFocus()
                if (holder.bindingAdapterPosition == data.size - 1) {
                    if (data.size == 1) {
                        setFocusListener.setFocus(-1)
                    } else {
                        setFocusListener.setFocus(holder.bindingAdapterPosition - 1)
                    }
                } else {
                    setFocusListener.setFocus(holder.bindingAdapterPosition + 1)
                }
            }
            data.removeAt(holder.bindingAdapterPosition)
            notifyItemRemoved(holder.bindingAdapterPosition)
        }

        if (holder.bindingAdapterPosition == itemCount - 1) {
            holder.editTextSubtaskTitle.post(Runnable {
                if (getFocus) {
                    holder.editTextSubtaskTitle.requestFocus()
                } else {
                    getFocus = true
                }
            })
        }
    }

    interface SetFocusListener {
        fun setFocus(position: Int)
    }
}
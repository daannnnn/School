package com.dan.school.fragments

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.R
import com.dan.school.adapters.SubtasksShowListAdapter
import com.dan.school.models.Subtask
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_subtasks_bottom_sheet.*


class SubtasksBottomSheetDialogFragment(
    private val subtasks: ArrayList<Subtask>
) : BottomSheetDialogFragment() {

    var float4dp = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_subtasks_bottom_sheet, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        float4dp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f,
            requireContext().resources.displayMetrics
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewSubtasks.layoutManager = LinearLayoutManager(context)
        recyclerViewSubtasks.adapter = SubtasksShowListAdapter(requireContext(), subtasks)

        dialog?.setOnShowListener {
            recyclerViewSubtasks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (recyclerViewSubtasks.canScrollVertically(-1)) {
                        appBar.elevation = float4dp
                    } else {
                        appBar.elevation = 0f
                    }
                    Log.i("Test", "called")
                }
            })
        }
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(IntArray(0), ObjectAnimator.ofFloat(view, "elevation", 0f))
        appBar.stateListAnimator = stateListAnimator
    }
}
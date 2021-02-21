package com.dan.school.ui.fragments

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dan.school.other.DataViewModel
import com.dan.school.adapters.SubtasksShowListAdapter
import com.dan.school.databinding.LayoutSubtasksBottomSheetBinding
import com.dan.school.models.Subtask
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson

private const val ELEVATION = "elevation"

class SubtasksBottomSheetDialogFragment(
    private val subtasks: ArrayList<Subtask>,
    private val itemTitle: String,
    private val itemId: Int,
    private val uncheckedIcon: Int,
    private val checkedIcon: Int
) : BottomSheetDialogFragment(), SubtasksShowListAdapter.SubtaskChangedListener {

    private var _binding: LayoutSubtasksBottomSheetBinding? = null

    private val binding get() = _binding!!

    var float4dp = 0f
    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutSubtasksBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.recyclerViewSubtasks.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewSubtasks.adapter = SubtasksShowListAdapter(
            requireContext(), subtasks,
            this,
            uncheckedIcon, checkedIcon
        )
        binding.textViewItemTitle.text = itemTitle

        dialog?.setOnShowListener {
            binding.recyclerViewSubtasks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (binding.recyclerViewSubtasks.canScrollVertically(-1)) {
                        binding.appBar.elevation = float4dp
                    } else {
                        binding.appBar.elevation = 0f
                    }
                }
            })
        }
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(IntArray(0), ObjectAnimator.ofFloat(view, ELEVATION, 0f))
        binding.appBar.stateListAnimator = stateListAnimator
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun subtaskChanged() {
        dataViewModel.setItemSubtasks(itemId, Gson().toJson(subtasks))
    }
}
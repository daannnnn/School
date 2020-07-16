package com.dan.school.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.DataViewModel
import com.dan.school.R
import com.dan.school.adapters.HomeworkListAdapter
import com.dan.school.models.Subtask
import kotlinx.android.synthetic.main.fragment_homeworks.*

class HomeworksFragment : Fragment(), HomeworkListAdapter.DoneListener,
    HomeworkListAdapter.ShowSubtasksListener {

    private lateinit var dataViewModel: DataViewModel
    private lateinit var homeworkListAdapter: HomeworkListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_homeworks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeworkListAdapter = HomeworkListAdapter(requireContext(), this, this)
        recyclerViewHomework.layoutManager = LinearLayoutManager(context)
        recyclerViewHomework.adapter = homeworkListAdapter

        dataViewModel.allHomeworks.observe(viewLifecycleOwner, Observer { homeworks ->
            homeworks?.let { homeworkListAdapter.submitList(it) }
        })
    }

    override fun setDone(id: Int, done: Boolean) {
        dataViewModel.setDone(id, done)
    }

    override fun showSubtasks(subtasks: ArrayList<Subtask>, itemTitle: String, id: Int) {
        SubtasksBottomSheetDialogFragment(subtasks, itemTitle, id).show(childFragmentManager, "subtasksBottomSheet")
    }
}
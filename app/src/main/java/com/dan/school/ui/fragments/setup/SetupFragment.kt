package com.dan.school.ui.fragments.setup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.databinding.FragmentSetupBinding
import com.dan.school.ui.activities.SetupActivity

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null

    private val binding get() = _binding!!

    private lateinit var buttonGetStartedClickListener: ButtonGetStartedClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is SetupActivity) {
            buttonGetStartedClickListener = activity as SetupActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGetStarted.setOnClickListener {
            buttonGetStartedClickListener.buttonGetStartedClicked()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface ButtonGetStartedClickListener {
        fun buttonGetStartedClicked()
    }

}
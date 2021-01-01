package com.dan.school.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.R
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : Fragment() {

    private lateinit var welcomeDoneButtonClickListener: WelcomeDoneButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is AuthenticationActivity) {
            welcomeDoneButtonClickListener = activity as AuthenticationActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonDone.setOnClickListener {
            welcomeDoneButtonClickListener.welcomeDoneButtonClicked()
        }
    }

    interface WelcomeDoneButtonClickListener {
        fun welcomeDoneButtonClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            WelcomeFragment()
    }
}
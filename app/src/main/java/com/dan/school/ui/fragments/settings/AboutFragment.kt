package com.dan.school.ui.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dan.school.BuildConfig
import com.dan.school.R
import com.dan.school.databinding.FragmentAboutBinding
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder


class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val version = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        binding.textViewVersion.text = version

        binding.settingsItemLicenses.setOnClickListener {
            showLibrariesPage()
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun showLibrariesPage() {
        LibsBuilder()
            .withFields(R.string::class.java.fields)
            .withActivityTitle(getString(R.string.licenses))
            .withLicenseShown(true)
            .withAboutIconShown(false)
            .withLibraryModification(
                getString(R.string.com_github_antonyt__InfiniteViewPager_library),
                Libs.LibraryFields.LIBRARY_DESCRIPTION,
                getString(R.string.com_github_antonyt__InfiniteViewPager_description)
            )
            .withLibraryModification(
                getString(R.string.com_github_antonyt__InfiniteViewPager_library),
                Libs.LibraryFields.LIBRARY_WEBSITE,
                getString(R.string.com_github_antonyt__InfiniteViewPager_libraryWebsite)
            )
            .withLibraryModification(
                getString(R.string.com_github_kizitonwose__CalendarView_library),
                Libs.LibraryFields.LIBRARY_DESCRIPTION,
                getString(R.string.com_github_kizitonwose__CalendarView_description)
            )
            .withLibraryModification(
                getString(R.string.com_github_kizitonwose__CalendarView_library),
                Libs.LibraryFields.LIBRARY_WEBSITE,
                getString(R.string.com_github_kizitonwose__CalendarView_libraryWebsite)
            )
            .start(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
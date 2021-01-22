package com.dan.school.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dan.school.BuildConfig
import com.dan.school.R
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val version = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
        textViewVersion.text = version

        relativeLayoutLicenses.setOnClickListener {
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

        buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

}
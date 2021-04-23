package com.dan.school.ui.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.dan.school.R
import com.dan.school.other.School
import com.dan.school.databinding.ActivitySettingsBinding
import com.dan.school.ui.fragments.settings.AboutFragment
import com.dan.school.ui.fragments.settings.BackupFragment
import com.dan.school.ui.fragments.settings.ProfileFragment
import com.dan.school.ui.fragments.settings.SettingsFragment

class SettingsActivity : AppCompatActivity(), SettingsFragment.SettingsItemOnClickListener {

    private lateinit var binding: ActivitySettingsBinding

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutSettings,
                    SettingsFragment.newInstance()
                ).commit()
        }
    }

    override fun itemClicked(item: Int) {
        when (item) {
            School.PROFILE -> {
                replaceFragment(ProfileFragment())
            }
            School.BACKUP -> {
                replaceFragment(BackupFragment())
            }
            School.ABOUT -> {
                replaceFragment(AboutFragment())
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(
                R.id.frameLayoutSettings,
                fragment
            ).addToBackStack(null)
            .commit()
    }
}
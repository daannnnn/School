package com.dan.school.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dan.school.BuildConfig
import com.dan.school.R
import com.dan.school.other.School
import com.dan.school.databinding.ActivitySetupBinding
import com.dan.school.ui.fragments.setup.ProfileSetupFragment
import com.dan.school.ui.fragments.setup.SetupFragment
import com.dan.school.ui.fragments.setup.SetupViewPagerFragment

class SetupActivity : AppCompatActivity(), ProfileSetupFragment.ProfileSetupDoneListener,
    SetupFragment.ButtonGetStartedClickListener, SetupViewPagerFragment.SetupDoneListener {

    private lateinit var binding: ActivitySetupBinding

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutSetup,
                    SetupFragment()
                ).commit()
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
                R.id.frameLayoutSetup,
                fragment
            )
            .addToBackStack(null)
            .commit()
    }

    override fun profileSetupDone(nickname: String, fullName: String) {
        saveUpdatedProfile(nickname, fullName)
        replaceFragment(SetupViewPagerFragment())
    }

    private fun saveUpdatedProfile(nickname: String, fullName: String) {
        with(sharedPref.edit()) {
            putString(School.NICKNAME, nickname)
            putString(School.FULL_NAME, fullName)
            apply()
        }
    }

    override fun buttonGetStartedClicked() {
        replaceFragment(ProfileSetupFragment.newInstance())
    }

    override fun setupDone() {
        with(sharedPref.edit()) {
            putBoolean(School.IS_SETUP_DONE, true)
            apply()
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
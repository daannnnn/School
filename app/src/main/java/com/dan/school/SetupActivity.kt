package com.dan.school

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dan.school.setup.ProfileSetupFragment
import com.dan.school.setup.SetupFragment
import com.dan.school.setup.SetupViewPagerFragment

class SetupActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

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

    fun buttonGetStartedClicked() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(
                R.id.frameLayoutSetup,
                ProfileSetupFragment()
            ).commit()
    }

    fun profileSetupDone(nickname: String, fullName: String) {
        with(sharedPref.edit()) {
            putString(School.NICKNAME, nickname)
            putString(School.FULL_NAME, fullName)
            apply()
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(
                R.id.frameLayoutSetup,
                SetupViewPagerFragment()
            ).commit()
    }

    fun setupDone() {
        with(sharedPref.edit()) {
            putBoolean(School.IS_SETUP_DONE, true)
            apply()
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
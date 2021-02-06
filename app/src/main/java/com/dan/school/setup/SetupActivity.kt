package com.dan.school.setup

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dan.school.BuildConfig
import com.dan.school.MainActivity
import com.dan.school.R
import com.dan.school.School
import com.dan.school.authentication.AuthenticationActivity
import com.dan.school.databinding.ActivitySetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class SetupActivity : AppCompatActivity(), ProfileSetupFragment.ProfileSetupDoneListener,
    SetupFragment.ButtonGetStartedClickListener, SetupViewPagerFragment.SetupDoneListener {

    private lateinit var binding: ActivitySetupBinding

    private lateinit var sharedPref: SharedPreferences

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.messaging.subscribeToTopic(School.UPDATES)
        Firebase.messaging.subscribeToTopic("v${BuildConfig.VERSION_NAME}")

        auth = Firebase.auth
        database = Firebase.database

        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (savedInstanceState == null) {
            if (auth.currentUser == null) {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.frameLayoutSetup,
                        SetupFragment()
                    ).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.frameLayoutSetup,
                        SetupViewPagerFragment()
                    ).commit()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTHENTICATION_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            authenticationComplete(data)
        }
    }

    private fun authenticationComplete(data: Intent?) {
        when (data?.getIntExtra(AuthenticationActivity.RESULT, -1)) {
            AuthenticationActivity.AUTHENTICATION_CANCELLED -> {
                replaceFragment(ProfileSetupFragment.newInstance())
            }
            AuthenticationActivity.AUTHENTICATION_SUCCESS -> {
                replaceFragment(SetupViewPagerFragment())
            }
            AuthenticationActivity.AUTHENTICATION_WITH_GOOGLE_SUCCESS -> {
                replaceFragment(
                    ProfileSetupFragment.newInstance(
                        data.getStringExtra(School.NICKNAME) ?: "",
                        data.getStringExtra(School.FULL_NAME) ?: ""
                    )
                )
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frameLayoutSetup,
                fragment
            ).commit()
    }

    override fun buttonGetStartedClicked() {
        startActivityForResult(
            Intent(this, AuthenticationActivity::class.java),
            AUTHENTICATION_ACTIVITY_REQUEST_CODE
        )
    }

    override fun profileSetupDone(nickname: String, fullName: String) {
        saveUpdatedProfile(nickname, fullName)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(
                R.id.frameLayoutSetup,
                SetupViewPagerFragment()
            ).commit()
    }

    private fun saveUpdatedProfile(nickname: String, fullName: String) {
        with(sharedPref.edit()) {
            putString(School.NICKNAME, nickname)
            putString(School.FULL_NAME, fullName)
            putBoolean(School.DATABASE_PROFILE_UPDATED, false)
            apply()
        }

        if (auth.currentUser != null) {
            val map: MutableMap<String, Any> = HashMap()
            map[School.NICKNAME] = nickname
            map[School.FULL_NAME] = fullName
            map[School.PROFILE_LAST_UPDATE_TIME] = ServerValue.TIMESTAMP
            database.reference.child(School.USERS).child(auth.currentUser!!.uid)
                .updateChildren(map)
                .addOnSuccessListener {
                    sharedPref.edit().putBoolean(School.DATABASE_PROFILE_UPDATED, true).apply()
                }
        }
    }

    override fun setupDone() {
        with(sharedPref.edit()) {
            putBoolean(School.IS_SETUP_DONE, true)
            apply()
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        const val AUTHENTICATION_ACTIVITY_REQUEST_CODE = 1
    }
}
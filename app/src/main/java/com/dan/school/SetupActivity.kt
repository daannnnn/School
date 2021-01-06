package com.dan.school

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dan.school.authentication.AuthenticationActivity
import com.dan.school.setup.ProfileSetupFragment
import com.dan.school.setup.SetupFragment
import com.dan.school.setup.SetupViewPagerFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_setup.*
import java.net.InetAddress

class SetupActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var isProgressBarVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

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
            when (data?.getIntExtra(AuthenticationActivity.RESULT, -1)) {
                AuthenticationActivity.AUTHENTICATION_CANCELLED -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayoutSetup,
                            ProfileSetupFragment.newInstance()
                        ).commit()
                }
                AuthenticationActivity.AUTHENTICATION_SUCCESS -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(
                            R.id.frameLayoutSetup,
                            SetupViewPagerFragment()
                        ).commit()
                }
                AuthenticationActivity.AUTHENTICATION_WITH_GOOGLE_SUCCESS -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayoutSetup,
                            ProfileSetupFragment.newInstance(
                                data.getStringExtra(School.NICKNAME) ?: "",
                                data.getStringExtra(School.FULL_NAME) ?: ""
                            )
                        ).commit()
                }
            }
        }
    }

    fun buttonGetStartedClicked() {
        startActivityForResult(
            Intent(this, AuthenticationActivity::class.java),
            AUTHENTICATION_ACTIVITY_REQUEST_CODE
        )
    }

    fun profileSetupDone(nickname: String, fullName: String) {
        showProgressBar()
        with(sharedPref.edit()) {
            putString(School.NICKNAME, nickname)
            putString(School.FULL_NAME, fullName)
            apply()
        }
        if (isInternetAvailable()) {
            if (auth.currentUser != null) {
                val map: MutableMap<String, Any> = HashMap()
                map[School.NICKNAME] = nickname
                map[School.FULL_NAME] = fullName
                database.reference.child(School.USERS).child(auth.currentUser!!.uid)
                    .updateChildren(map).addOnCompleteListener {
                        hideProgressBar()
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(
                                R.id.frameLayoutSetup,
                                SetupViewPagerFragment()
                            ).commit()
                    }
            }
        } else {
            hideProgressBar()
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(
                    R.id.frameLayoutSetup,
                    SetupViewPagerFragment()
                ).commit()
        }
    }

    fun setupDone() {
        with(sharedPref.edit()) {
            putBoolean(School.IS_SETUP_DONE, true)
            apply()
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showProgressBar() {
        Utils.hideKeyboard(this)
        isProgressBarVisible = true
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        groupProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        groupProgressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        isProgressBarVisible = false
    }

    private fun isInternetAvailable(): Boolean {
        return try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            !ipAddr.equals("")
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val AUTHENTICATION_ACTIVITY_REQUEST_CODE = 1
    }
}
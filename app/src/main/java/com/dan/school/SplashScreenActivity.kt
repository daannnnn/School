package com.dan.school

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dan.school.authentication.AuthenticationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        /**
         * Show [SetupActivity] if is first launch, else [MainActivity]
         */
        if (getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            ).getBoolean(School.IS_SETUP_DONE, false)
        ) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
        }
    }
}
package com.dan.school

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
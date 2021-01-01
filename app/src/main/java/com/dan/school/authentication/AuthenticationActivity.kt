package com.dan.school.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dan.school.R
import com.dan.school.School


class AuthenticationActivity : AppCompatActivity(),
    AuthenticationFragment.ButtonSignInWithClickListener,
    AuthenticationFragment.ButtonSignUpClickListener,
    AuthenticationFragment.ButtonSignInLaterClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutAuthentication,
                    AuthenticationFragment.newInstance()
                ).commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    private fun buttonSignInWithEmailClicked() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(
                R.id.frameLayoutAuthentication,
                SignInFragment.newInstance()
            ).addToBackStack(null)
            .commit()
    }

    private fun buttonSignInWithGoogleClicked() {
        TODO()
    }

    private fun goToSignUp() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(
                R.id.frameLayoutAuthentication,
                SignUpFragment.newInstance()
            ).addToBackStack(null)
            .commit()
    }

    private fun done(result: Int) {
        val returnIntent = Intent()
        returnIntent.putExtra(RESULT, result)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun buttonSignInWithClicked(signInWith: Int) {
        if (signInWith == School.SIGN_IN_WITH_EMAIL) {
            buttonSignInWithEmailClicked()
        } else if (signInWith == School.SIGN_IN_WITH_GOOGLE) {
            buttonSignInWithGoogleClicked()
        }
    }

    override fun buttonSignUpClicked() {
        goToSignUp()
    }

    override fun buttonSignInLaterClicked() {
        done(AUTHENTICATION_CANCELLED)
    }

    companion object {
        const val AUTHENTICATION_CANCELLED = 0
        const val AUTHENTICATION_SUCCESS = 1

        const val RESULT = "result"
    }

}
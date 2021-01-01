package com.dan.school.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dan.school.R
import com.dan.school.School
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthenticationActivity : AppCompatActivity(),
    AuthenticationFragment.ButtonSignInWithClickListener,
    AuthenticationFragment.ButtonSignUpClickListener,
    AuthenticationFragment.ButtonSignInLaterClickListener,
    SignUpFragment.SignUpButtonClickListener, WelcomeFragment.WelcomeDoneButtonClickListener,
    SignInFragment.SignInButtonClickListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        auth = Firebase.auth

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

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(
                            R.id.frameLayoutAuthentication,
                            WelcomeFragment.newInstance()
                        ).commit()
                } else {
                    Toast.makeText(
                        baseContext, "Sign up failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    done(AUTHENTICATION_SUCCESS)
                } else {
                    Toast.makeText(
                        baseContext, "Sign in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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

    fun goToFP() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(
                R.id.frameLayoutAuthentication,
                ResetPasswordFragment.newInstance()
            ).addToBackStack(null)
            .commit()
    }

    companion object {
        const val AUTHENTICATION_CANCELLED = 0
        const val AUTHENTICATION_SUCCESS = 1

        const val RESULT = "result"
    }

    override fun signUpButtonClicked(email: String, password: String) {
        createUser(email, password)
    }

    override fun welcomeDoneButtonClicked() {
        done(AUTHENTICATION_SUCCESS)
    }

    override fun signInButtonClicked(email: String, password: String) {
        signInUser(email, password)
    }

}
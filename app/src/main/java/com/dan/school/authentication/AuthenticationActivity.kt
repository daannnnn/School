package com.dan.school.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dan.school.R
import com.dan.school.School
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthenticationActivity : AppCompatActivity(),
    AuthenticationFragment.ButtonSignInWithClickListener,
    AuthenticationFragment.ButtonSignUpClickListener,
    AuthenticationFragment.ButtonSignInLaterClickListener,
    SignUpFragment.SignUpButtonClickListener, WelcomeFragment.WelcomeDoneButtonClickListener,
    SignInFragment.SignInButtonClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutAuthentication,
                    AuthenticationFragment.newInstance()
                ).commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_WITH_GOOGLE_RC) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Sign in failed. Please try again.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    done(AUTHENTICATION_SUCCESS)
                } else {
                    Toast.makeText(this, "Sign in failed. Please try again.", Toast.LENGTH_SHORT)
                        .show()
                }
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
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGN_IN_WITH_GOOGLE_RC)
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
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener {
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Failed to send verification email. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            supportFragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                                .replace(
                                    R.id.frameLayoutAuthentication,
                                    WelcomeFragment.newInstance(it.isSuccessful, email)
                                ).commit()
                        }
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this, "User already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            this, "Invalid email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(
                            this, "Password is too weak.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this, "Sign up failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    done(AUTHENTICATION_SUCCESS)
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            this, "Invalid email or password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this, "Sign in failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
        const val SIGN_IN_WITH_GOOGLE_RC = 2

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
package com.dan.school.authentication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.dan.school.R
import com.dan.school.School
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AuthenticationActivity : AppCompatActivity(),
    AuthenticationFragment.ButtonSignInWithClickListener,
    AuthenticationFragment.ButtonSignUpClickListener,
    AuthenticationFragment.ButtonSignInLaterClickListener,
    SignUpFragment.SignUpButtonClickListener, WelcomeFragment.WelcomeDoneButtonClickListener,
    SignInFragment.SignInButtonClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        auth = Firebase.auth
        database = Firebase.database

        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

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
                firebaseAuthWithGoogle(
                    account.idToken!!,
                    account.givenName ?: "",
                    account.displayName ?: ""
                )
            } catch (e: ApiException) {
                Toast.makeText(this, "Sign in failed. Please try again.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed: ")
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            val myFragment: WelcomeFragment? =
                supportFragmentManager.findFragmentByTag("welcomeFragment") as WelcomeFragment?
            if (myFragment != null && myFragment.isVisible) {
                done(AUTHENTICATION_SUCCESS)
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, nickname: String, fullName: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    done(AUTHENTICATION_WITH_GOOGLE_SUCCESS, nickname, fullName)
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

    private fun createUser(email: String, password: String, nickname: String, fullName: String) {
        // start progressbar
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { createUserTask ->
                if (createUserTask.isSuccessful) {
                    if (auth.currentUser != null) {
                        updateProfileOnSharedPreferences(nickname, fullName)
                        updateProfileOnFirebaseDatabase(email, nickname, fullName)
                    }

                } else {
                    try {
                        throw createUserTask.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this, "User already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(
                            this, "Password is too weak.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            this, "Invalid email.",
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

    private fun updateProfileOnFirebaseDatabase(email: String, nickname: String, fullName: String) {
        val map: MutableMap<String, Any> = HashMap()
        map[School.NICKNAME] = nickname
        map[School.FULL_NAME] = fullName
        database.reference.child(School.USERS).child(auth.currentUser!!.uid)
            .updateChildren(map).addOnCompleteListener {
                sendEmailVerification(email)
            }
    }

    private fun updateProfileOnSharedPreferences(nickname: String, fullName: String) {
        sharedPref.edit {
            putString(School.NICKNAME, nickname)
            putString(School.FULL_NAME, fullName)
            commit()
        }
    }

    private fun sendEmailVerification(email: String) {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Failed to send verification email. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // stop progressbar

                for (i in 0 until supportFragmentManager.backStackEntryCount) {
                    supportFragmentManager.popBackStack()
                }
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(
                        R.id.frameLayoutAuthentication,
                        WelcomeFragment.newInstance(it.isSuccessful, email),
                        "welcomeFragment"
                    ).commit()
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

    private fun done(result: Int, nickname: String = "", fullName: String = "") {
        val returnIntent = Intent()
        returnIntent.putExtra(RESULT, result)
        returnIntent.putExtra(School.NICKNAME, nickname)
        returnIntent.putExtra(School.FULL_NAME, fullName)
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

    override fun signUpButtonClicked(
        email: String,
        password: String,
        nickname: String,
        fullName: String
    ) {
        createUser(email, password, nickname, fullName)
    }

    override fun welcomeDoneButtonClicked() {
        done(AUTHENTICATION_SUCCESS)
    }

    override fun signInButtonClicked(email: String, password: String) {
        signInUser(email, password)
    }

    companion object {
        const val AUTHENTICATION_CANCELLED = 0
        const val AUTHENTICATION_SUCCESS = 1
        const val AUTHENTICATION_WITH_GOOGLE_SUCCESS = 2
        const val SIGN_IN_WITH_GOOGLE_RC = 3

        const val RESULT = "result"

        const val TAG = "AuthenticationActivity"
    }

}
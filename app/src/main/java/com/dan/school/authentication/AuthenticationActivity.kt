package com.dan.school.authentication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.dan.school.ProgressBarDialog
import com.dan.school.R
import com.dan.school.School
import com.dan.school.Utils
import com.dan.school.databinding.ActivityAuthenticationBinding
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
    SignInFragment.SignInButtonClickListener, SignInFragment.ForgotPasswordButtonClickListener {

    private lateinit var binding: ActivityAuthenticationBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPref: SharedPreferences

    private lateinit var progressBarDialog: ProgressBarDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBarDialog = ProgressBarDialog(this)

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
                    AuthenticationFragment.newInstance(intent.getBooleanExtra(School.SHOW_BUTTON_SIGN_IN_LATER, true))
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
                hideProgressBar()
                Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            val myFragment: WelcomeFragment? =
                supportFragmentManager.findFragmentByTag(WELCOME_FRAGMENT) as WelcomeFragment?
            if (myFragment != null && myFragment.isVisible) {
                done(AUTHENTICATION_SUCCESS)
            } else {
                super.onBackPressed()
            }
        }
    }

    /**
     * Signs in user on [Firebase.auth] using sign in with google.
     */
    private fun firebaseAuthWithGoogle(idToken: String, nickname: String, fullName: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    done(AUTHENTICATION_WITH_GOOGLE_SUCCESS, nickname, fullName)
                } else {
                    Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT)
                        .show()
                }
                hideProgressBar()
            }
    }

    /**
     * Changes fragment to [SignInFragment].
     */
    private fun buttonSignInWithEmailClicked() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(
                R.id.frameLayoutAuthentication,
                SignInFragment.newInstance()
            ).addToBackStack(null)
            .commit()
    }

    /**
     * Starts an intent for sign in with google.
     */
    private fun buttonSignInWithGoogleClicked() {
        showProgressBar()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGN_IN_WITH_GOOGLE_RC)
    }

    /**
     * Changes fragment to [SignUpFragment].
     */
    private fun goToSignUp() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(
                R.id.frameLayoutAuthentication,
                SignUpFragment.newInstance()
            ).addToBackStack(null)
            .commit()
    }

    /**
     * Changes fragment to [ResetPasswordFragment]
     */
    private fun goToResetPassword() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(
                R.id.frameLayoutAuthentication,
                ResetPasswordFragment.newInstance()
            ).addToBackStack(null)
            .commit()
    }

    /**
     * Creates a new user using [Firebase.auth].
     */
    private fun createUser(email: String, password: String, nickname: String, fullName: String) {
        showProgressBar()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { createUserTask ->
                if (createUserTask.isSuccessful) {
                    if (auth.currentUser != null) {
                        sharedPref.edit().putBoolean(School.DATABASE_PROFILE_UPDATED, false).apply()
                        updateProfileOnSharedPreferences(nickname, fullName)
                        updateProfileOnFirebaseDatabase(nickname, fullName)
                        sendEmailVerification(email)
                    }
                } else {
                    hideProgressBar()
                    try {
                        throw createUserTask.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this, getString(R.string.user_already_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(
                            this, getString(R.string.password_is_too_weak),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            this, getString(R.string.invalid_email),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this, getString(R.string.sign_up_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    /**
     * Updates user's [nickname] and [fullName] on [Firebase.database].
     */
    private fun updateProfileOnFirebaseDatabase(nickname: String, fullName: String) {
        val map: MutableMap<String, Any> = HashMap()
        map[School.NICKNAME] = nickname
        map[School.FULL_NAME] = fullName
        database.reference.child(School.USERS).child(auth.currentUser!!.uid)
            .updateChildren(map)
            .addOnSuccessListener {
                sharedPref.edit().putBoolean(School.DATABASE_PROFILE_UPDATED, true).apply()
            }
    }

    /**
     * Updates user's [nickname] and [fullName] on [sharedPref].
     */
    private fun updateProfileOnSharedPreferences(nickname: String, fullName: String) {
        sharedPref.edit {
            putString(School.NICKNAME, nickname)
            putString(School.FULL_NAME, fullName)
            commit()
        }
    }

    /**
     * Sends an email verification to [email].
     */
    private fun sendEmailVerification(email: String) {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener {
                hideProgressBar()
                if (!it.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.failed_to_send_verification_email),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                for (i in 0 until supportFragmentManager.backStackEntryCount) {
                    supportFragmentManager.popBackStack()
                }
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(
                        R.id.frameLayoutAuthentication,
                        WelcomeFragment.newInstance(it.isSuccessful, email),
                        WELCOME_FRAGMENT
                    ).commit()
            }
    }

    /**
     * Signs in a user with [email] and [password]
     */
    private fun signInUser(email: String, password: String) {
        showProgressBar()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    done(AUTHENTICATION_SUCCESS)
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            this, getString(R.string.invalid_email_or_password),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this, getString(R.string.sign_in_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                hideProgressBar()
            }
    }

    /**
     * Finishes activity and sends result data.
     *
     * [result] One of [AUTHENTICATION_CANCELLED], [AUTHENTICATION_SUCCESS],
     * or [AUTHENTICATION_WITH_GOOGLE_SUCCESS].
     */
    private fun done(result: Int, nickname: String = "", fullName: String = "") {
        val returnIntent = Intent()
        returnIntent.putExtra(RESULT, result)
        returnIntent.putExtra(School.NICKNAME, nickname)
        returnIntent.putExtra(School.FULL_NAME, fullName)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    private fun showProgressBar() {
        Utils.hideKeyboard(this)
        progressBarDialog.show()
    }

    private fun hideProgressBar() {
        progressBarDialog.hide()
    }

    /**
     * Calls [buttonSignInWithEmailClicked] or [buttonSignInWithGoogleClicked]
     * depending on [signInWith].
     *
     * [signInWith] One of [School.SIGN_IN_WITH_EMAIL] or
     * [School.SIGN_IN_WITH_GOOGLE].
     */
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

    override fun forgotPasswordButtonClicked() {
        goToResetPassword()
    }

    companion object {
        const val AUTHENTICATION_CANCELLED = 0
        const val AUTHENTICATION_SUCCESS = 1
        const val AUTHENTICATION_WITH_GOOGLE_SUCCESS = 2
        const val SIGN_IN_WITH_GOOGLE_RC = 3

        const val RESULT = "result"

        const val WELCOME_FRAGMENT = "welcomeFragment"
    }

}
package com.dan.school

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dan.school.fragments.CompletedFragment
import com.dan.school.fragments.OverviewFragment
import com.dan.school.settings.SettingsActivity
import com.dan.school.settings.SettingsFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OverviewFragment.OpenDrawerListener,
    OverviewFragment.ClickCounterListener {

    /**
     * Saves the last click time from FAB, BottomNavigationView, and
     * from an item click.
     */
    private var lastClickTime = System.currentTimeMillis()

    /**
     * Saves the amount of clicks from FAB, BottomNavigationView, and
     * from an item click.
     */
    private var clickCounter = 0

    private var onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener? = null

    private lateinit var interstitialAd: InterstitialAd

    private lateinit var sharedPref: SharedPreferences

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Firebase.messaging.subscribeToTopic(School.UPDATES)
        Firebase.messaging.subscribeToTopic("v${BuildConfig.VERSION_NAME}")

        MobileAds.initialize(this) {}

        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        interstitialAd.loadAd(AdRequest.Builder().build())

        auth = Firebase.auth
        database = Firebase.database

        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (savedInstanceState == null) {  // to prevent multiple creation of instances
            navigationView.menu.getItem(0).isChecked = true
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutMain,
                    OverviewFragment(), School.OVERVIEW
                ).commit()
        }

        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (auth.currentUser != null) {
            database.reference.child(School.USERS).child(auth.currentUser!!.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val map = HashMap<String, String>()
                        for (dataSnapshot in snapshot.children) {
                            val key = dataSnapshot.key
                            if (key == School.NICKNAME || key == School.FULL_NAME) {
                                map[key] = dataSnapshot.value.toString()
                            }
                        }
                        updateSharedPreferences(map)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
        setNavigationViewHeaderNickname(sharedPref.getString(School.NICKNAME, ""))
        setNavigationViewHeaderFullName(sharedPref.getString(School.FULL_NAME, ""))

        // listeners
        onSharedPreferenceChangeListener =
            OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == School.FULL_NAME) {
                    setNavigationViewHeaderFullName(
                        sharedPreferences.getString(
                            School.FULL_NAME,
                            ""
                        )
                    )
                }
                if (key == School.NICKNAME) {
                    setNavigationViewHeaderNickname(
                        sharedPreferences.getString(
                            School.NICKNAME,
                            ""
                        )
                    )
                }
            }
        sharedPref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        navigationView.setNavigationItemSelectedListener { item ->
            if (!item.isChecked) {
                drawerLayout.closeDrawers()

                when (item.itemId) {
                    R.id.overview -> {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        if (supportFragmentManager.backStackEntryCount != 0) {
                            supportFragmentManager.popBackStackImmediate()
                        }
                    }
                    R.id.completed -> {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        supportFragmentManager.beginTransaction()
                            .add(R.id.frameLayoutMain, CompletedFragment(), School.COMPLETED)
                            .addToBackStack(School.COMPLETED)
                            .commit()
                    }
                    R.id.settings -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                }
            } else {
                drawerLayout.closeDrawers()
            }
            return@setNavigationItemSelectedListener true
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                    showFragment(School.OVERVIEW)
                    navigationView.setCheckedItem(R.id.overview)
                }
            } else {
                if (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name
                    == School.COMPLETED
                ) {
                    if (supportFragmentManager.findFragmentByTag(School.COMPLETED) != null) {
                        showFragment(School.COMPLETED)
                        navigationView.setCheckedItem(R.id.completed)
                    }
                    if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                        supportFragmentManager.beginTransaction()
                            .hide(supportFragmentManager.findFragmentByTag(School.OVERVIEW)!!)
                            .commit()
                    }
                }
            }
        }
    }

    /** Sets [R.id.textViewNickname] text to [nickname] */
    private fun setNavigationViewHeaderNickname(nickname: String?) {
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.textViewNickname).text =
            nickname
    }

    /** Sets [R.id.textViewFullName] text to [fullName] */
    private fun setNavigationViewHeaderFullName(fullName: String?) {
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.textViewFullName).text =
            fullName
    }

    /** Shows fragment with tag [tag] */
    private fun showFragment(tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag)!!.isVisible) {
            return
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, 0)
            .show(supportFragmentManager.findFragmentByTag(tag)!!).commit()
    }

    /**
     * Updates [clickCounter] if the difference between
     * [System.currentTimeMillis] and [lastClickTime] in seconds
     * is greater than 0.5 seconds then shows [interstitialAd] if
     * loaded and if [clickCounter] is greater than or equal to 8
     *
     * Returns true if show is called on [interstitialAd], false
     * otherwise
     */
    private fun updateCounter(): Boolean {
        var b = false
        if ((System.currentTimeMillis() - lastClickTime) / 1000 > 0.5) {
            clickCounter++
            lastClickTime = System.currentTimeMillis()
            if (clickCounter >= 5) {
                if (interstitialAd.isLoaded) {
                    interstitialAd.show()
                    clickCounter = 0
                    b = true
                }
            }
        }
        return b
    }

    private fun updateSharedPreferences(map: HashMap<String, String>) {
        val editor = sharedPref.edit()
        for (item in map) {
            editor.putString(item.key, item.value)
        }
        editor.apply()
    }

    override fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    /**
     * Calls [callback] immediately if [updateCounter] returns
     * false, otherwise wait for ad to close before calling
     * [callback]
     */
    override fun incrementCounter(callback: () -> Unit) {
        if (updateCounter()) {
            interstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    interstitialAd.loadAd(AdRequest.Builder().build())
                    callback()
                }
            }
        } else {
            callback()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else super.onBackPressed()
    }
}
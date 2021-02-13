package com.dan.school

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dan.school.databinding.ActivityMainBinding
import com.dan.school.fragments.CompletedFragment
import com.dan.school.fragments.OverviewFragment
import com.dan.school.settings.SettingsActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

typealias Callback = () -> Unit

class MainActivity : AppCompatActivity(), OverviewFragment.OpenDrawerListener,
    OverviewFragment.ClickCounterListener {

    private lateinit var binding: ActivityMainBinding

    /**
     * Saves the last time show is called on [interstitialAd]
     */
    private var lastAdShowTime = System.currentTimeMillis()

    /**
     * Saves the amount of clicks from FAB, BottomNavigationView, and
     * from an item click.
     */
    private var clickCounter = 0

    private var onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener? = null

    private var interstitialAd: InterstitialAd? = null

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.messaging.subscribeToTopic(School.UPDATES)
        Firebase.messaging.subscribeToTopic("v${BuildConfig.VERSION_NAME}")

        MobileAds.initialize(this) {}

        loadAd()

        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        if (savedInstanceState == null) {  // to prevent multiple creation of instances
            binding.navigationView.menu.getItem(0).isChecked = true
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayoutMain,
                    OverviewFragment(), School.OVERVIEW
                ).commit()
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
        binding.navigationView.setNavigationItemSelectedListener { item ->
            if (!item.isChecked) {
                binding.drawerLayout.closeDrawers()

                when (item.itemId) {
                    R.id.overview -> {
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        if (supportFragmentManager.backStackEntryCount != 0) {
                            supportFragmentManager.popBackStackImmediate()
                        }
                    }
                    R.id.completed -> {
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
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
                binding.drawerLayout.closeDrawers()
            }
            return@setNavigationItemSelectedListener true
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                if (supportFragmentManager.findFragmentByTag(School.OVERVIEW) != null) {
                    showFragment(School.OVERVIEW)
                    binding.navigationView.setCheckedItem(R.id.overview)
                }
            } else {
                if (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name
                    == School.COMPLETED
                ) {
                    if (supportFragmentManager.findFragmentByTag(School.COMPLETED) != null) {
                        showFragment(School.COMPLETED)
                        binding.navigationView.setCheckedItem(R.id.completed)
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

    private fun loadAd() {
        InterstitialAd.load(
            this,
            getString(R.string.interstitial1Id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
            }
        )
    }

    /** Sets [R.id.textViewNickname] text to [nickname] */
    private fun setNavigationViewHeaderNickname(nickname: String?) {
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.textViewNickname).text =
            nickname
    }

    /** Sets [R.id.textViewFullName] text to [fullName] */
    private fun setNavigationViewHeaderFullName(fullName: String?) {
        binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.textViewFullName).text =
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
     * Updates [clickCounter] then shows [interstitialAd] if
     * loaded, if [clickCounter] is greater than or equal to 5
     * and if the difference between [System.currentTimeMillis] and
     * [lastAdShowTime] in seconds is greater than 60.
     *
     * Returns true if show is called on [interstitialAd], false
     * otherwise
     */
    private fun updateCounter(): Boolean {
        var b = false
        clickCounter++
        if (clickCounter >= 5 && (System.currentTimeMillis() - lastAdShowTime) / 1000 > 60) {
            if (interstitialAd != null) {
                interstitialAd?.show(this)
                lastAdShowTime = System.currentTimeMillis()
                clickCounter = 0
                b = true
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
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    /**
     * Calls [callback] immediately if [updateCounter] returns
     * false, otherwise wait for ad to close before calling
     * [callback]
     */
    override fun incrementCounter(callback: () -> Unit) {
        if (updateCounter()) {

            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadAd()
                    callback()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                    callback()
                }
                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
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
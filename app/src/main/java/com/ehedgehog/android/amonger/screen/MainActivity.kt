package com.ehedgehog.android.amonger.screen

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.screen.playersList.PlayersListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val storedThemeMode = PlayersPreferences.getStoredThemeMode(this)
        val mode = if (storedThemeMode == PlayersListViewModel.ThemeMode.NIGHT) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser == null) {
            tryAuthAnonymously()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun tryAuthAnonymously() {
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("AuthTest", "Auth successful")
            } else {
                Log.i("AuthTest", "Auth failed")
                AlertDialog.Builder(this)
                    .setTitle("Oops")
                    .setMessage(getString(R.string.auth_failed_message))
                    .setPositiveButton("Retry") { _, _ -> tryAuthAnonymously() }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

}
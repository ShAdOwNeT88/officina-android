package com.officinasocialeproarpaia.officina_android.features.main

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.officinasocialeproarpaia.officina_android.R
import com.officinasocialeproarpaia.officina_android.databinding.ActivityMainBinding
import com.officinasocialeproarpaia.officina_android.utils.exhaustive
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainScreen : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val permissionRequestFineLocation = 1
    private val permissionRequestBackgroundLocation = 2

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionCheck()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(ACCESS_BACKGROUND_LOCATION)) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.permission_request_title))
                        builder.setMessage(getString(R.string.permission_request_description))
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener { requestPermissions(arrayOf(ACCESS_BACKGROUND_LOCATION), permissionRequestBackgroundLocation) }
                        builder.show()
                    } else {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.permission_denied_title))
                        builder.setMessage(getString(R.string.permission_denied_description))
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener { }
                        builder.show()
                    }
                }
            } else {
                if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                    requestPermissions(arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION), permissionRequestFineLocation)
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.permission_denied_title))
                    builder.setMessage(getString(R.string.permission_denied_description))
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            permissionRequestFineLocation -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d(this.packageName, "fine location permission granted")
                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener(DialogInterface.OnDismissListener { })
                    builder.show()
                }
                return
            }
            permissionRequestBackgroundLocation -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d(this.packageName, "background location permission granted")
                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener(DialogInterface.OnDismissListener { })
                    builder.show()
                }
                return
            }
        }
    }
}

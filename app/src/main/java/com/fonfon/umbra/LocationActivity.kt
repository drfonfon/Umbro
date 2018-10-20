package com.fonfon.umbra

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

@SuppressLint("Registered")
open class LocationActivity : AppCompatActivity() {

    private val tag = LocationActivity::class.java.name
    private val requestLocationSettings = 1
    private val requestLocation = 2

    private val coarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val fineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permGranted = PackageManager.PERMISSION_GRANTED

    private val locationRequest = LocationRequest.create().also {
        it.interval = 0L
        it.fastestInterval = 0L
        it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val locationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .build()

    lateinit var locationCallback: LocationCallback

    private var permissionGranted = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    protected var content: View? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            requestLocationSettings -> when (resultCode) {
                Activity.RESULT_OK -> startLocationUpdates()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        startLocation()
        hideBars();
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == requestLocation)
            permissionGranted = (grantResults.size == 2
                    && grantResults[0] == permGranted
                    && grantResults[1] == permGranted)
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestLocationPermission() {
        val locationCoarse = ActivityCompat.shouldShowRequestPermissionRationale(this, coarseLocation)
        val locationFine = ActivityCompat.shouldShowRequestPermissionRationale(this, fineLocation)

        if (locationCoarse && locationFine)
            AlertDialog.Builder(this)
                .setMessage("Need Location Permission")
                .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                .setPositiveButton(android.R.string.ok) { _, _ -> locationPermissionRequest() }
                .show()
        else
            locationPermissionRequest()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (checkFine() && checkCoarse()) {
            requestLocationPermission()
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun startLocation() {
        permissionGranted = checkCoarse() && checkFine()
        if (permissionGranted) requestLocationPermission()
        else LocationServices.getSettingsClient(this)
            .checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                startLocationUpdates()
            }
            .addOnFailureListener {
                if (it is ResolvableApiException) try {
                    it.startResolutionForResult(this@LocationActivity, requestLocationSettings)
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    Log.i(tag, "PendingIntent unable to execute request.")
                }
            }
    }

    private fun locationPermissionRequest() {
        ActivityCompat.requestPermissions(this@LocationActivity, arrayOf(coarseLocation, fineLocation), requestLocation)
    }

    private fun checkFine() = ActivityCompat.checkSelfPermission(this@LocationActivity, fineLocation) != permGranted
    private fun checkCoarse() = ActivityCompat.checkSelfPermission(this@LocationActivity, coarseLocation) != permGranted

    @SuppressLint("InlinedApi")
    protected fun hideBars() {
        content?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}

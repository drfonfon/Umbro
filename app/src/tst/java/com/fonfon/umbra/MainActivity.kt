package com.fonfon.umbra

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import com.fonfon.umbra.compass.BearingToNorthProvider
import com.fonfon.umbra.data.Infected
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.tst.activity_main.*

class MainActivity : LocationActivity(), OnMapReadyCallback {

    val Location.latlng
        get() = LatLng(latitude, longitude)

    lateinit var presenter: PresenterMain
    var init = true

    lateinit var userMarker: Marker

    lateinit var bearingToNorthProvider: BearingToNorthProvider

    var map: GoogleMap? = null

    var infectedMarker: Marker? = null
    var infectedCircle: Circle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = PresenterMain(this)

        bearingToNorthProvider = BearingToNorthProvider(this, 50, 5.0, 300)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        presenter.onPortal = { loc ->
            map?.let {
                it.addMarker(
                    MarkerOptions()
                        .position(loc.latlng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.exit))
                        .anchor(0.5f, 0.5f)
                )

                it.addCircle(
                    CircleOptions().center(loc.latlng).radius(4.0)
                        .fillColor(Color.parseColor("#990000FF"))
                        .strokeWidth(0.0f)
                )
            }
        }

        presenter.onEnemy = { loc ->
            map?.let {
                it.addMarker(
                    MarkerOptions()
                        .position(loc.latlng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.monster))
                        .anchor(0.5f, 0.5f)
                )
                it.addCircle(
                    CircleOptions().center(loc.latlng).radius(4.0)
                        .fillColor(Color.parseColor("#88000000"))
                        .strokeWidth(0.0f)
                )
            }
        }

        presenter.onLocation = { loc ->
            map?.let {
                if (init) {
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(loc.latlng, it.maxZoomLevel - 1))
                    userMarker = it.addMarker(
                        MarkerOptions()
                            .flat(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation))
                            .position(loc.latlng)
                    )
                    init = false
                } else {
                    userMarker.position = loc.latlng
                }
            }
        }

        presenter.onAzimuth = {

            if (!init) {
                userMarker.rotation = it.toFloat()
            }
        }

        presenter.onInfected = { loc ->
            if (infectedMarker == null) {
                map?.let {
                    infectedMarker = it.addMarker(
                        MarkerOptions()
                            .position(loc.latlng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.monster2))
                            .anchor(0.5f, 0.5f)
                    )
                    infectedCircle = it.addCircle(
                        CircleOptions().center(loc.latlng).radius(4.0)
                            .fillColor(Color.parseColor("#88FF0000"))
                            .strokeWidth(0.0f)
                    )
                }
            } else {
                infectedMarker?.position = loc.latlng
                infectedCircle?.center = loc.latlng
            }
        }

        presenter.win = {
            AlertDialog.Builder(this)
                .setMessage("You win!")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    restartApp()
                }
                .show()
        }

        presenter.died = {
            AlertDialog.Builder(this)
                .setMessage("You LOOOZE!")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    restartApp()
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
        bearingToNorthProvider.start()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
        bearingToNorthProvider.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map
        map?.let {
            presenter.gameStart()
        }
    }

    private fun restartApp() {
        val mStartActivity = Intent(this, MainActivity::class.java)
        val mPendingIntentId = 123456
        val mPendingIntent = PendingIntent.getActivity(
            this, mPendingIntentId, mStartActivity,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)
    }
}

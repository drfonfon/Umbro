package com.fonfon.umbra

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import com.fonfon.umbra.compass.BearingToNorthProvider
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
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                it.addCircle(
                    CircleOptions().center(loc.latlng).radius(4.0).fillColor(
                        Color.parseColor("#aa0000FF")
                    )
                )
            }
        }

        presenter.onEnemy = { loc ->
            map?.let {
                it.addMarker(
                    MarkerOptions()
                        .position(loc.latlng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
                it.addCircle(
                    CircleOptions().center(loc.latlng).radius(4.0).fillColor(
                        Color.parseColor(
                            "#aa00FF00"
                        )
                    )
                )
            }
        }

        presenter.onLocation = {loc ->
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
            compassView.azimuth = it
            compassView.invalidate()
            text_st.text = it.toString()

            if (!init) {
                userMarker.rotation = it.toFloat()
            }
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
}

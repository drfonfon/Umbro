package com.fonfon.umbra

import android.content.Context
import android.location.Location
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import com.crickettechnology.audio.AttenuationMode
import com.crickettechnology.audio.Ck
import com.crickettechnology.audio.Sound
import com.fonfon.umbra.compass.BearingToNorthProvider
import com.fonfon.umbra.data.Effects
import com.fonfon.umbra.data.Holemohster
import com.fonfon.umbra.data.Player
import com.fonfon.umbra.data.Portal
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import java.util.*



class PresenterMain(val activity: LocationActivity) {

    val r = Random().nextInt(LocationsGenerator.countMax)
    val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    val bearingToNorthProvider = BearingToNorthProvider(activity, 50, 5.0, 300)

    lateinit var effects: Effects

    var start = false
    var init = true

    var died = {}

    var win = {}

    var onPortal = { loc: Location -> }
    var onEnemy = { loc: Location -> }
    var onLocation = { loc: Location -> }
    var onAzimuth = { az: Double -> }

    lateinit var portal: Portal

    lateinit var location: Location
    var azimuth: Double = 0.0

    val enemies = ArrayList<Holemohster>()

    init {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Ck.init(activity)
        Sound.set3dAttenuation(AttenuationMode.Linear, 0.5f, 15f, 0.1f)

        activity.locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                result?.lastLocation?.let {
                    location = it
                    bearingToNorthProvider.onLocationChanged(it)
                    onLocation(it)

                    if (start) {
                        portal.location?.let { loc ->
                            val p = portal.process(location, bearingToNorthProvider.azimuth, bearingToNorthProvider.declination)
                            if (p.first) {
                                stopGame(true)
                            }
                            if (p.second) {
                                vibrate()
                            }
                            for (monster in enemies) {
                                if (monster.process(
                                        it,
                                        bearingToNorthProvider.azimuth,
                                        bearingToNorthProvider.declination
                                    )
                                ) {
                                    stopGame(false)
                                }
                            }
                        }

                        if (init) {
                            val locs = LocationsGenerator.genetateLocations(it)
                            for (loc in locs) {
                                if (locs.indexOf(loc) == r) {
                                    portal.location = loc
                                    onPortal(loc)
                                } else {
                                    val monster = Holemohster()
                                    monster.location = loc
                                    enemies.add(monster)
                                    onEnemy(loc)
                                }

                            }
                            init = false
                        }
                    }
                }
            }
        }

        bearingToNorthProvider.changeEventListener = {
            azimuth = it
            onAzimuth(it)

            if (start) {
                portal.location?.let {
                    val p = portal.process(location, bearingToNorthProvider.azimuth, bearingToNorthProvider.declination)
                    if (p.first) {
                        stopGame(true)
                    }
                    if (p.second) {
                        vibrate()
                    }

                    for (monster in enemies) {
                        if (monster.process(it, bearingToNorthProvider.azimuth, bearingToNorthProvider.declination)) {
                            stopGame(false)
                        }
                    }
                }
            }
        }
    }

    fun gameStart() {
        effects = Effects()
        portal = Portal()

        effects.start()

        start = true
        init = true
    }

    fun resume() {
        Ck.resume()
        if (start) {
            effects.start()
        }
        bearingToNorthProvider.start()
    }

    fun pause() {
        Ck.suspend()
        effects.stop()
        bearingToNorthProvider.stop()
    }

    fun destroy() {
        Ck.shutdown()
    }

    fun stopGame(win: Boolean) {
        if (win) {
            this.win()
            Player().win.play()
        } else {
            this.died()
            Player().death.play()
        }
        effects.stop()
        start = false
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v?.vibrate(100)
        }
    }
}

package com.fonfon.umbra

import android.content.Context
import android.location.Location
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import com.crickettechnology.audio.AttenuationMode
import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Ck
import com.crickettechnology.audio.Sound
import com.fonfon.arduino.Serial
import com.fonfon.umbra.LocationsGenerator.random
import com.fonfon.umbra.compass.BearingToNorthProvider
import com.fonfon.umbra.data.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import java.util.*


class PresenterMain(val activity: LocationActivity) {

    val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    val bearingToNorthProvider = BearingToNorthProvider(activity, 50, 5.0, 300)

    private var effects: Effects
    private var dog: Dog
    private var ambient: Ambient

    var start = false
    var init = true

    var died = {}

    var win = {}

    var onPortal = { loc: Location -> }
    var onInfected = { loc: Location -> }
    var onEnemy = { loc: Location -> }
    var onLocation = { loc: Location -> }
    var onAzimuth = { az: Double -> }

    lateinit var portal: Portal
    var serial: Serial

    lateinit var location: Location
    private var azimuth: Double = 0.0

    val enemies = ArrayList<HoleMonster>()
    lateinit var infected: Infected

    init {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Ck.init(activity)
        Sound.set3dAttenuation(AttenuationMode.Linear, 0.5f, 15f, 0.1f)

        effects = Effects()
        serial = Serial(activity)
        ambient = Ambient()
        dog = Dog()

        activity.locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                result?.lastLocation?.let {
                    location = it
                    bearingToNorthProvider.onLocationChanged(it)
                    onLocation(it)

                    if (start) {
                        portal.location?.let { loc ->
                            val p = portal.process(
                                location,
                                bearingToNorthProvider.azimuth,
                                bearingToNorthProvider.declination
                            )
                            if (p.first) {
                                stopGame(true)
                            }
                            if (p.second) {
                                serial.resumePulse()
                                vibrate()
                            } else {
                                serial.pausePulse()
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
                            if (infected.process(
                                    it,
                                    bearingToNorthProvider.azimuth,
                                    bearingToNorthProvider.declination
                                )
                            ) {
                                stopGame(false)
                            }
                            onInfected(infected.location!!)
                        }

                        if (init) {
                            init(it)
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
                        serial.resumePulse()
                        vibrate()
                    } else {
                        serial.pausePulse()
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
        portal = Portal()
        infected = Infected()
        effects.start()
        ambient.start()
        dog.start()
        start = true
        init = true
    }

    fun resume() {
        Ck.resume()
        if (start) {
            effects.start()
            ambient.start()
        }
        serial.resume()
        bearingToNorthProvider.start()
    }

    fun pause() {
        Ck.suspend()
        effects.stop()
        dog.stop()
        ambient.stop()
        bearingToNorthProvider.stop()
    }

    fun destroy() {
        Ck.shutdown()
        serial.stopPulse()
    }

    fun stopGame(win: Boolean) {
        if (win) {
            this.win()
            Thread { Sound.newBankSound(Bank.newBank("player.ckb"), 0).play() }.start()
            serial.winCommand()
        } else {
            this.died()
            Thread { Sound.newBankSound(Bank.newBank("player.ckb"), 1).play() }.start()
            serial.loseCommand()
        }
        effects.stop()
        dog.stop()
        ambient.stop()
        start = false
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v?.vibrate(100)
        }
    }

    fun init(startLoc: Location) {
        val locs = LocationsGenerator.generateLocations(startLoc)

        //find portal
        var ndis = startLoc.distanceTo(locs[0])
        var nloc = locs[0]

        for(loc in locs) {
            val d = startLoc.distanceTo(loc)
            if (d > ndis) {
                ndis = d
                nloc = loc
            }
        }

        portal.location = nloc
        onPortal(nloc)
        serial.startPulse()

        locs.remove(nloc)

        //find run monster
        ndis = startLoc.distanceTo(locs[0])
        nloc = locs[0]

        for(loc in locs) {
            val d = startLoc.distanceTo(loc)
            if (d > ndis) {
                ndis = d
                nloc = loc
            }
        }

        infected.location = nloc
        onInfected(nloc)

        locs.remove(nloc)

        for (loc in locs) {
            val monster = HoleMonster()
            monster.location = loc
            enemies.add(monster)
            onEnemy(loc)
        }
    }
}

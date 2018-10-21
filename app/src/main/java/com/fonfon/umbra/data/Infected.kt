package com.fonfon.umbra.data

import android.location.Location
import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Sound

class Infected {

    val bank = Bank.newBank("infected.ckb")
    val amb = Sound.newBankSound(bank, 0).apply {
        this.is3dEnabled = true
    }
    val attack = Sound.newBankSound(bank, 1).apply {
        this.is3dEnabled = true
        this.volume = 1.0f
    }

    var location: Location? = null

    fun process(user: Location, azimuth: Double, declination: Float): Boolean {
        location?.let {
            location = step(it, user, 5)
        }
        var bearTo = user.bearingTo(location)
        if (bearTo < 0) bearTo += 360

        var rotation = bearTo - azimuth - declination
        if (rotation < 0) rotation += 360
        if (rotation > 360) rotation -= 360
        if (rotation > 180) rotation -= 360

        val dist = user.distanceTo(location)

        Thread {
            val x = -1 * dist * Math.cos(Math.toRadians(rotation - 90))
            val y = -1 * dist * Math.sin(Math.toRadians(rotation - 90))

            if (dist != 0.0f) {
                if (dist < 4 && dist > 2) {
                    val pos = amb.playPosition
                    amb.stop()
                    amb.volume = if (dist < 4) dist / 4 else 0.05f
                    amb.set3dPosition(x.toFloat(), y.toFloat(), 0f)
                    amb.playPosition = pos
                    amb.play()
                } else if (dist < 2) {
                    amb.stop()
                    attack.set3dPosition(x.toFloat(), y.toFloat(), 0f)
                    attack.play()
                }
            }
        }.start()

        return dist != 0.0f && dist < 2
    }

    fun step(from: Location, to: Location, stepCount: Int) : Location {
        var pair: Pair<Double, Double> = loco(from.latitude, from.longitude, to.latitude, to.longitude)
        for (i in 0 until stepCount) {
            pair = loco(from.latitude, from.longitude, pair.first, pair.second)
        }
        return Location("umbra").apply {
            latitude = pair.first
            longitude = pair.second
        }
    }

    fun loco(fromLa: Double, fomLo: Double, toLa: Double, toLo: Double) = Pair((fromLa + toLa) / 2, (fomLo + toLo) / 2)
}

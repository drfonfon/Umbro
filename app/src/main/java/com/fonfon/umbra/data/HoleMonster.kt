package com.fonfon.umbra.data

import android.location.Location
import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Sound

class HoleMonster {

    val bank = Bank.newBank("holemonster.ckb")
    val amb = Sound.newBankSound(bank, 0).apply {
        this.is3dEnabled = true
    }
    val attack = Sound.newBankSound(bank, 1).apply {
        this.is3dEnabled = true
        this.volume = 1.0f
    }

    var location: Location? = null

    fun process(user: Location, azimuth: Double, declination: Float): Boolean {
        var bearTo = user.bearingTo(location)
        if (bearTo < 0) bearTo += 360

        var rotation = bearTo - azimuth - declination
        if (rotation < 0) rotation += 360
        if (rotation > 360) rotation -= 360
        if (rotation > 180) rotation -= 360

        val dist = user.distanceTo(location)

        Thread {
            var x = -1 * dist * Math.cos(Math.toRadians(rotation - 90))
            val y = -1 * dist * Math.sin(Math.toRadians(rotation - 90))

            if ((rotation > -15 && rotation < 15) || (rotation < -165 && rotation > -165) || (rotation > 165 && rotation < 165)) {
                x = 0.0
            }

            if (dist != 0.0f || dist > 4) {
                if (dist < 4 && dist > 2) {
                    val pos = amb.playPosition
                    amb.stop()
                    amb.volume = (if (dist < 4) dist / 4 else 0.0f) / 2
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


}
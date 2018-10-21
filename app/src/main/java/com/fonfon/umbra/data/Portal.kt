package com.fonfon.umbra.data

import android.location.Location
import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Sound

class Portal {

    var sound = Sound.newBankSound(Bank.newBank("portal.ckb"), 0).apply {
        this.is3dEnabled = true
        this.loopCount = -1
    }
    var location: Location? = null

    fun process(user: Location, azimuth: Double, declination: Float): Pair<Boolean, Boolean> {

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

            val pos = sound.playPosition
            sound.stop()
            sound.volume = if (dist < 10) dist / 10 else 0.1f
            sound.set3dPosition(x.toFloat(), y.toFloat(), 0f)
            sound.playPosition = pos
            sound.play()
        }.start()

        return Pair(dist < 4, (rotation > -15 && rotation < 15))
    }
}

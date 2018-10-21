package com.fonfon.umbra.data

import android.os.Handler
import android.os.Looper
import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Sound
import com.fonfon.umbra.LocationsGenerator.random
import java.util.*

class Dog {

    val sound = Sound.newBankSound(Bank.newBank("dog.ckb"), 0).apply {
        this.is3dEnabled = true
        this.volume = 0.05f
    }

    val handler = Handler()
    val runable = object : Runnable {
        override fun run() {
            sound.set3dPosition((random.nextInt(100) - 50).toFloat(), 0f, 0f)
            sound.play()
            handler.postDelayed(this, (random.nextInt(50000) + 10000 + sound.lengthMs).toLong())
        }
    }

    fun start() {
        handler.postDelayed(runable, (random.nextInt(50000) + 10000).toLong())
    }

    fun stop() {
        handler.removeCallbacks(runable)
    }
}
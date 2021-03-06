package com.fonfon.umbra.data

import android.os.Handler
import android.os.Looper
import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Sound
import com.fonfon.umbra.LocationsGenerator.random
import java.util.*

class Effects {

    val bank = Bank.newBank("zvuki.ckb")

    val handler = Handler()
    val runable = object : Runnable {
        override fun run() {
            val sound = Sound.newBankSound(bank, random.nextInt(bank.numSounds)).apply {
                this.is3dEnabled = true
                this.volume = 0.1f
                this.set3dPosition(0f, 5f, 200f)
            }
            sound.play()
            handler.postDelayed(this, (random.nextInt(100) + 10000 + sound.lengthMs).toLong())
        }
    }

    fun start() {
        handler.postDelayed(runable, (random.nextInt(100) + 10000).toLong())
    }

    fun stop() {
        handler.removeCallbacks(runable)
    }
}
package com.fonfon.umbra.data

import android.os.Handler
import android.os.Looper
import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Sound
import java.util.*

class Effects {

    val random = Random()

    val bank = Bank.newBank("zvuki.ckb")

    val randomEffect: Sound
    get() = Sound.newBankSound(bank, random.nextInt(bank.numSounds)).apply {
        this.is3dEnabled = true
        this.volume = 0.5f
        this.set3dPosition(0f, 5f, 100f)
    }


    val handler = Handler(Looper.getMainLooper())
    val runable = object : Runnable {
        override fun run() {
            val sound = randomEffect
            sound.play()
            handler.postDelayed(this, (random.nextInt(10000) + 10000 + sound.lengthMs).toLong())
        }
    }

    fun start() {
        handler.postDelayed(runable, (random.nextInt(10000) + 10000).toLong())
    }

    fun stop() {
        handler.removeCallbacks(runable)
    }
}
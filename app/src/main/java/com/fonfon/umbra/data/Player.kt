package com.fonfon.umbra.data

import com.crickettechnology.audio.Bank
import com.crickettechnology.audio.Sound

class Player {

    val bank = Bank.newBank("player.ckb")
    val win = Sound.newBankSound(bank, 0)
    val death = Sound.newBankSound(bank, 1)
}

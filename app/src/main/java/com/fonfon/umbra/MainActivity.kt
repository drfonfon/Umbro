package com.fonfon.umbra

import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fonfon.umbra.hardware.SerialUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUsbDevice()
    }

    private fun setupUsbDevice() {
        textview.text = "setupUsbDevice"

        val filter = IntentFilter().apply {
            addAction(SerialUtil.ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        registerReceiver(SerialUtil.broadcastReceiver, filter)

        SerialUtil.connect(this)
        SerialUtil.onConnectionListener = { onConnect() }
    }

    private fun onConnect() {
        textview.text = "onConnect"

//        Thread({
//            SerialUtil.portalLED()
//            Thread.sleep(200)
//            SerialUtil.offLED()
//            Thread.sleep(200)
//        }).start()
        SerialUtil.offLED()
    }
}

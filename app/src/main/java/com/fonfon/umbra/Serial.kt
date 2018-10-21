package com.fonfon.arduino

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class Serial(private val activity: AppCompatActivity) {

    val ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION"
    var usbManager: UsbManager = activity.getSystemService(USB_SERVICE) as UsbManager

    var device: UsbDevice? = null
    var serialPort: UsbSerialDevice? = null
    var connection: UsbDeviceConnection? = null

    private val broadcastReceiver =
        object : BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_USB_PERMISSION) {
                    val granted = intent.extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                    if (granted) {
                        connection = usbManager.openDevice(device)
                        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
                        serialPort?.let {
                            if (it.open()) { //Set Serial Connection Parameters.
                                it.setBaudRate(9600)
                                it.setDataBits(UsbSerialInterface.DATA_BITS_8)
                                it.setStopBits(UsbSerialInterface.STOP_BITS_1)
                                it.setParity(UsbSerialInterface.PARITY_NONE)
                                it.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                            }
                        }
                    }
                } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                    onClickStart()
                } else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                    if (serialPort != null) {
                        serialPort?.close()
                    }
                }
            }
        }

    init {
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        activity.registerReceiver(broadcastReceiver, filter)
    }

    private fun write(b: Int) {
        serialPort?.write(byteArrayOf(b.toByte()))
    }

    fun startPulse() {
        write(0)
    }

    fun resumePulse() {
        write(1)
    }

    fun pausePulse() {
        write(2)
    }

    fun winCommand() {
        write(3)
    }

    fun loseCommand() {
        write(4)
    }

    fun stopPulse() {
        write(5)
    }

    fun resume() {
        onClickStart()
    }

    fun onClickStart() {
        val usbDevices = usbManager.deviceList
        if (!usbDevices.isEmpty()) {
            var keep = true
            for (entry in usbDevices.entries) {
                device = entry.value
                val deviceVID = device?.vendorId
                if (deviceVID == 0x2341) {
                    val pi = PendingIntent.getBroadcast(activity, 0, Intent(ACTION_USB_PERMISSION), 0)
                    usbManager.requestPermission(device, pi)
                    keep = false
                } else {
                    connection = null
                    device = null
                }

                if (!keep)
                    break
            }
        }
    }

}
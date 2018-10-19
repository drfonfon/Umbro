package com.fonfon.umbra.hardware

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface


/**
 * @author Kirill Chirkin
 * @since 19.10.2018, 21:29.
 */
object SerialUtil {

    const val ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION"
    private var usbManager: UsbManager? = null
    private var usbDevice: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var serialPort: UsbSerialDevice? = null

    var onConnectionListener: (() -> Unit)? = null

    fun connect(context: Context) {
        usbManager = context.getSystemService(USB_SERVICE) as UsbManager

        val usbDevices = usbManager?.deviceList
        usbDevices?.let {
            if (!usbDevices.isEmpty()) {
                var keep = true
                for (entry in usbDevices.entries) {
                    usbDevice = entry.value
                    usbDevice?.let { device ->
                        val deviceVID = device.vendorId
                        if (deviceVID == 0x2341) {
                            val pi = PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
                            usbManager?.requestPermission(device, pi)
                            keep = false
                        } else {
                            connection = null
                            usbDevice = null
                        }
                    }

                    if (!keep)
                        break
                }
            }
        }
    }

    val broadcastReceiver = object : BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val granted = intent.extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted) {
                    connection = usbManager?.openDevice(usbDevice)
                    serialPort = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection)
                    serialPort?.let { sp ->
                        if (sp.open()) { //Set Serial Connection Parameters.
                            sp.setBaudRate(9600)
                            sp.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            sp.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            sp.setParity(UsbSerialInterface.PARITY_NONE)
                            sp.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                        }
                    }

                    onConnectionListener?.invoke()
                }

            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                connect(context)
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                serialPort?.close()
            }
        }
    }

    fun portalLED() {
        serialPort?.write(byteArrayOf(1))
    }

    fun offLED() {
        serialPort?.write(byteArrayOf(2))
    }

}
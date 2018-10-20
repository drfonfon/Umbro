package com.fonfon.umbra.compass

import android.content.Context
import android.hardware.*
import android.location.Location

class BearingToNorthProvider
/**
 * @param context         Application Context
 * @param smoothing       the number of measurements used to calculate a mean for the azimuth. Set
 * this to 1 for the smallest delay. Setting it to 5-10 to prevents the
 * needle from going crazy
 * @param minDiffForEvent minimum change of bearing (degrees) to notify the change listener
 * @param throttleTime    minimum delay (millis) between notifications for the change listener
 */
@JvmOverloads constructor(
    context: Context,
    smoothing: Int = 10,
    private val mMinDiffForEvent: Double = 0.5,
    throttleTime: Int = 50
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val temporaryRotationMatrix = FloatArray(9)
    private val valuesAccelerometer = FloatArray(3)
    private val valuesMagneticField = FloatArray(3)
    private val matrixR = FloatArray(9)
    private val matrixI = FloatArray(9)
    private val matrixValues = FloatArray(3)
    private val throttleTime = throttleTime.toDouble()
    private val azimuthRadians = AverageAngle(smoothing)
    var azimuth = Double.NaN
    var declination = 0f
    private var lastBearing = Double.NaN
    private var location: Location? = null
    private var lastChangeDispatchedAt = -1L

    var changeEventListener: (Double) -> Unit = { bearing -> }
    var bearing = Double.NaN

    fun start() {
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this, sensorAccelerometer)
        sensorManager.unregisterListener(this, sensorMagneticField)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, valuesAccelerometer, 0, 3)
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, valuesMagneticField, 0, 3)
        }

        val success =
            SensorManager.getRotationMatrix(temporaryRotationMatrix, matrixI, valuesAccelerometer, valuesMagneticField)
        SensorManager.remapCoordinateSystem(
            temporaryRotationMatrix,
            SensorManager.AXIS_Z,
            SensorManager.AXIS_Y,
            matrixR
        )
        if (success) {
            SensorManager.getOrientation(matrixR, matrixValues)
            azimuthRadians.putValue(matrixValues[0].toDouble())
            azimuth = Math.toDegrees(azimuthRadians.average)
        }

        updateBearing()
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}

    fun onLocationChanged(location: Location) {
        // set the new location
        this.location = location

        // update mBearing
        updateBearing()
    }

    private fun updateBearing() {
        if (!java.lang.Double.isNaN(this.azimuth)) {
            bearing = azimuth

            location?.let {
                declination = it.geomagneticField.declination
                bearing += it.geomagneticField.declination
            }

            // Throttle dispatching based on mThrottleTime and minDiffForEvent
            if (System.currentTimeMillis() - lastChangeDispatchedAt > throttleTime && (java.lang.Double.isNaN(
                    lastBearing
                ) || Math.abs(lastBearing - bearing) >= mMinDiffForEvent)
            ) {
                lastBearing = bearing
                changeEventListener(bearing)
                lastChangeDispatchedAt = System.currentTimeMillis()
            }
        }
    }

    private val Location.geomagneticField: GeomagneticField
        get() = GeomagneticField(
            latitude.toFloat(),
            longitude.toFloat(),
            altitude.toFloat(),
            System.currentTimeMillis()
        )

}

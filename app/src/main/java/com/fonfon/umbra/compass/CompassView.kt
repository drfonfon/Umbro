package com.fonfon.umbra.compass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.util.AttributeSet
import android.view.View


class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  val other = Location("umbra").apply {
    latitude = 53.1685565
    longitude = 45.0087816
  }

  val paintBlack = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.STROKE
    color = Color.BLACK
    strokeWidth = 4f
  }

  val paintRed = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.STROKE
    color = Color.RED
    strokeWidth = 4f
  }

  var azimuth = 0.0
  var angle = 0.0

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    val sx = (width / 2).toFloat()
    val sy = (height / 2).toFloat()
    canvas.drawCircle(sx, sy, sx, paintBlack)

//    canvas.save()
//    canvas.rotate(-90f, sx, sy)
    val x = sx + sx * Math.cos(Math.toRadians(azimuth - 90))
    val y = sy + sy * Math.sin(Math.toRadians(azimuth - 90))
    canvas.drawLine(sx, sy, x.toFloat(), y.toFloat(), paintBlack)
//    canvas.restore()

//    canvas.save()
//    canvas.rotate(-180f, sx, sy)
    val x0 = sx + sx * Math.cos(Math.toRadians(angle - 90))
    val y0 = sy + sy * Math.sin(Math.toRadians(angle - 90))
    canvas.drawLine(sx, sy, x0.toFloat(), y0.toFloat(), paintRed)
//    canvas.restore()
  }
}
package com.fonfon.umbra.compass

class AverageAngle(private val numberOfFrames: Int) {

  private val values = DoubleArray(numberOfFrames)
  private var currentIndex = 0
  private var full = false
  var average = Double.NaN

  fun putValue(d: Double) {
    values[currentIndex] = d
    if (currentIndex == numberOfFrames - 1) {
      currentIndex = 0
      full = true
    } else {
      currentIndex++
    }
    updateAverageValue()
  }

  private fun updateAverageValue() {
    var numberOfElementsToConsider = numberOfFrames
    if (!full)
      numberOfElementsToConsider = currentIndex + 1

    if (numberOfElementsToConsider == 1) {
      this.average = values[0]
      return
    }

    // Formula: http://en.wikipedia.org/wiki/Circular_mean
    var sumSin = 0.0
    var sumCos = 0.0
    for (i in 0 until numberOfElementsToConsider) {
      val v = values[i]
      sumSin += Math.sin(v)
      sumCos += Math.cos(v)
    }
    this.average = Math.atan2(sumSin, sumCos)
  }
}
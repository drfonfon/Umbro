package com.fonfon.umbra

import android.location.Location
import java.util.*
import kotlin.collections.ArrayList

object LocationsGenerator {

  val radius = 15
  val countMax = 10
  val random = Random()

  private fun randomLocation(loc: Location): Location {

    // Convert radius from meters to degrees
    val radiusInDegrees = (radius / 111000f).toDouble()

    val u = random.nextDouble()
    val v = random.nextDouble()
    val w = radiusInDegrees * Math.sqrt(u)
    val t = 2.0 * Math.PI * v
    val x = w * Math.cos(t)
    val y = w * Math.sin(t)

    val new_x = x / Math.cos(Math.toRadians(loc.longitude))

    return Location("umbra").also {
      it.longitude = y + loc.longitude
      it.latitude = new_x + loc.latitude
    }
  }

  fun generateLocations(loc: Location): ArrayList<Location> {

    var count = 0
    val locations = ArrayList<Location>()

    while (count < countMax) {
      val location = randomLocation(loc)
      var add = true
      if (location.distanceTo(loc) < 9) {
        add = false
      }
      if (add) {
        for (l in locations) {
          if (location.distanceTo(l) < 5) {
            add = false
          }
        }
      }
      if (add) {
        locations.add(location)
        count++
      }
    }
    return locations
  }

}
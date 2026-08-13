package com.example.aegis.ui

import androidx.annotation.DrawableRes
import com.example.aegis.R

/** Maps zone ids to bundled imagery (keeps the domain model free of Android types). */
object ZoneArtwork {
  @DrawableRes
  fun imageFor(zoneId: String): Int =
    when (zoneId) {
      "cherrapunji" -> R.drawable.zone_cherrapunji
      "roots" -> R.drawable.zone_roots
      "dawki" -> R.drawable.zone_river
      "nohkalikai" -> R.drawable.zone_valley
      else -> R.drawable.zone_cherrapunji
    }

  @DrawableRes
  val rescuePostImage: Int = R.drawable.rescue_post
}

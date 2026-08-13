package com.example.aegis.safety

import com.example.aegis.domain.model.ZoneStatus

/** Pure, stateless risk evaluation logic (unit-tested). */
object RiskEvaluator {
  fun bandFor(score: Int): RiskBand = RiskBand.from(score.coerceIn(0, 100))

  fun toZoneStatus(score: Int): ZoneStatus =
    when (bandFor(score)) {
      RiskBand.SAFE -> ZoneStatus.SAFE
      RiskBand.CAUTION -> ZoneStatus.CAUTION
      RiskBand.HIGH_RISK -> ZoneStatus.HIGH_RISK
    }
}

package com.example.aegis.safety

import com.example.aegis.domain.model.ZoneStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskEvaluatorTest {

  @Test
  fun `score below 31 is Safe`() {
    assertEquals(RiskBand.SAFE, RiskEvaluator.bandFor(0))
    assertEquals(RiskBand.SAFE, RiskEvaluator.bandFor(15))
    assertEquals(RiskBand.SAFE, RiskEvaluator.bandFor(30))
    assertEquals(ZoneStatus.SAFE, RiskEvaluator.toZoneStatus(30))
  }

  @Test
  fun `score between 31 and 60 is Caution`() {
    assertEquals(RiskBand.CAUTION, RiskEvaluator.bandFor(31))
    assertEquals(RiskBand.CAUTION, RiskEvaluator.bandFor(45))
    assertEquals(RiskBand.CAUTION, RiskEvaluator.bandFor(60))
    assertEquals(ZoneStatus.CAUTION, RiskEvaluator.toZoneStatus(60))
  }

  @Test
  fun `score above 60 is High Risk`() {
    assertEquals(RiskBand.HIGH_RISK, RiskEvaluator.bandFor(61))
    assertEquals(RiskBand.HIGH_RISK, RiskEvaluator.bandFor(100))
    assertEquals(ZoneStatus.HIGH_RISK, RiskEvaluator.toZoneStatus(61))
  }

  @Test
  fun `out-of-range scores are clamped to valid bands`() {
    assertEquals(RiskBand.SAFE, RiskEvaluator.bandFor(-5))
    assertEquals(RiskBand.HIGH_RISK, RiskEvaluator.bandFor(150))
  }

  @Test
  fun `band boundaries are consistent with the design doc`() {
    // DESIGN.md risk semantics: 0-30 Safe, 31-60 Caution, 61+ High Risk.
    assertEquals(RiskBand.SAFE, RiskBand.from(30))
    assertEquals(RiskBand.CAUTION, RiskBand.from(31))
    assertEquals(RiskBand.CAUTION, RiskBand.from(60))
    assertEquals(RiskBand.HIGH_RISK, RiskBand.from(61))
  }
}

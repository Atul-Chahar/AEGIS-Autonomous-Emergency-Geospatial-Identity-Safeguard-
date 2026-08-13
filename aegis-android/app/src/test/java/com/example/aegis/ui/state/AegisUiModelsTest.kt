package com.example.aegis.ui.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AegisUiModelsTest {
  @Test
  fun guardianAttentionUsesTouristFacingCopy() {
    assertEquals("Guardian Attention", GuardianLevel.ATTENTION.title)
    assertEquals("Safety condition requires attention", GuardianLevel.ATTENTION.subtitle)
  }

  @Test
  fun sosStepOnlyShowsSuccessWhenSucceeded() {
    assertFalse(SosStepStatus.PENDING.showsSuccess)
    assertFalse(SosStepStatus.IN_PROGRESS.showsSuccess)
    assertTrue(SosStepStatus.SUCCEEDED.showsSuccess)
    assertFalse(SosStepStatus.FAILED.showsSuccess)
  }

  @Test
  fun significantDeviationUsesHumanDistanceCopy() {
    val state = RouteDeviationState(RouteDeviationSeverity.SIGNIFICANT, distanceMeters = 180)

    assertEquals("You are 180 m away from your planned trail.", state.message)
  }
}

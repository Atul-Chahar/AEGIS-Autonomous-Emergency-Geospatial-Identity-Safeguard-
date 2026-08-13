package com.example.aegis.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.aegis.theme.AEGISTheme
import org.junit.Rule
import org.junit.Test

/** UI smoke tests for the redesigned [HomeScreen]. */
class HomeScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun homeScreen_rendersCoreSafetyElements() {
    composeTestRule.setContent {
      AEGISTheme {
        HomeScreen(onOpenZones = {}, onOpenTouristId = {}, onOpenZoneDetail = {})
      }
    }
    composeTestRule.onNodeWithText("Cherrapunji Ridge").assertIsDisplayed()
    composeTestRule.onNodeWithText("Start Route").assertIsDisplayed()
    composeTestRule.onNodeWithText("TST-8F29X4").assertIsDisplayed()
  }
}

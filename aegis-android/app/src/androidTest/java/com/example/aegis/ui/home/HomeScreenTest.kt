package com.example.aegis.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.aegis.data.repository.demo.DemoIdentityRepository
import com.example.aegis.data.repository.demo.DemoSafetyZoneRepository
import com.example.aegis.domain.usecase.GetTouristIdentityUseCase
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
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
        HomeScreen(
          viewModel =
            HomeViewModel(
              observeZones = ObserveSafetyZonesUseCase(DemoSafetyZoneRepository()),
              observeIdentity = GetTouristIdentityUseCase(DemoIdentityRepository()),
            ),
          onOpenZones = {},
          onOpenTouristId = {},
          onOpenZoneDetail = {},
          onSos = {},
        )
      }
    }
    composeTestRule.onNodeWithText("Cherrapunji Ridge").assertIsDisplayed()
    composeTestRule.onNodeWithText("Start Route").assertIsDisplayed()
    composeTestRule.onNodeWithText("TST-8F29X4").assertIsDisplayed()
  }
}

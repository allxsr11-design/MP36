package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.PdfConfig
import com.example.ui.screens.ConvertScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ConvertScreen(
          pages = emptyList(),
          pdfConfig = PdfConfig(),
          isConverting = false,
          conversionProgress = 0f,
          conversionStatusText = "",
          onAddUris = {},
          onAddSamples = {},
          onRemovePage = {},
          onRotatePage = {},
          onClearAll = {},
          onSelectPageForEdit = {},
          onUpdateTitle = {},
          onUpdatePageSize = {},
          onUpdateOrientation = {},
          onUpdateMargin = {},
          onUpdateQuality = {},
          onUpdatePageNumberFormat = {},
          onToggleGrayscale = {},
          onStartConvert = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

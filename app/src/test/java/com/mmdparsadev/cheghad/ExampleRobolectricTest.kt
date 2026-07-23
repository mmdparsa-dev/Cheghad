package com.mmdparsadev.cheghad

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.mmdparsadev.cheghad.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    org.junit.Assert.assertTrue(appName == "چقد" || appName == "Cheghad")
  }

  @Test
  fun testThemeFontLoading() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Text("تست فونت وزیر")
      }
    }
  }
}

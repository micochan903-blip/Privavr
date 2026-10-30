package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.VrSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PrivaVR", appName)
  }

  @Test
  fun `verify vr settings defaults and fullscreen toggle`() {
    val settings = VrSettings()
    assertFalse(settings.isFullscreenVr)
    assertFalse(settings.isStereoCardboardMode)
    assertTrue(settings.headTrackingEnabled)

    val fullscreenSettings = settings.copy(isFullscreenVr = true)
    assertTrue(fullscreenSettings.isFullscreenVr)
  }
}

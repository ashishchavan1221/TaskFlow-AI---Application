package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.SecureSessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TaskFlow AI", appName)
  }

  @Test
  fun `secure session manager pin validation test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = SecureSessionManager(context)

    // Test valid 4-digit PIN setup and verification
    assertTrue(manager.setPin("1234"))
    assertTrue(manager.verifyPin("1234"))
    assertFalse(manager.verifyPin("9999"))

    // Test invalid non-4-digit PIN rejection
    assertFalse(manager.setPin("12"))
    assertFalse(manager.setPin("12345"))
    assertFalse(manager.setPin("abcd"))
  }
}

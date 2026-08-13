package com.example.aegis.data.local.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class BlackBoxEncryptorTest {

  private lateinit var encryptor: BlackBoxEncryptor

  @Before
  fun setup() {
    encryptor = BlackBoxEncryptor(isTestMode = true)
  }

  @Test
  fun `encryption and decryption cycle recovers original string`() {
    val samplePayload = """{"sensor":"IMPACT","x":12.4,"y":-3.8,"z":19.2}"""

    val encrypted = encryptor.encrypt(samplePayload)
    assertNotEquals(samplePayload, encrypted)

    val decrypted = encryptor.decrypt(encrypted)
    assertEquals(samplePayload, decrypted)
  }

  @Test
  fun `encrypting empty string returns empty string`() {
    assertEquals("", encryptor.encrypt(""))
    assertEquals("", encryptor.decrypt(""))
  }
}

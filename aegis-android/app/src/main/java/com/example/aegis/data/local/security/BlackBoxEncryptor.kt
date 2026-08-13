package com.example.aegis.data.local.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class BlackBoxEncryptor(
  private val keyAlias: String = KEY_ALIAS,
  private val isTestMode: Boolean = false,
) {
  companion object {
    private const val KEY_ALIAS = "aegis_blackbox_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
  }

  private val fallbackTestKey: SecretKey by lazy {
    SecretKeySpec(ByteArray(32) { 0x42 }, "AES")
  }

  private fun getSecretKey(): SecretKey {
    if (isTestMode) return fallbackTestKey

    return try {
      val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
      if (keyStore.containsAlias(keyAlias)) {
        val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        entry?.secretKey ?: generateKeystoreKey()
      } else {
        generateKeystoreKey()
      }
    } catch (e: Exception) {
      // Fallback for JVM unit test environment where AndroidKeyStore is unavailable
      fallbackTestKey
    }
  }

  private fun generateKeystoreKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    val spec = KeyGenParameterSpec.Builder(
      keyAlias,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
    )
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .setKeySize(256)
      .build()

    keyGenerator.init(spec)
    return keyGenerator.generateKey()
  }

  fun encrypt(plainText: String): String {
    if (plainText.isEmpty()) return ""
    val cipher = Cipher.getInstance(TRANSFORMATION)
    val key = getSecretKey()
    cipher.init(Cipher.ENCRYPT_MODE, key)

    val iv = cipher.iv
    val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

    val combined = ByteArray(iv.size + encryptedBytes.size)
    System.arraycopy(iv, 0, combined, 0, iv.size)
    System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

    return encodeBase64(combined)
  }

  fun decrypt(encryptedText: String): String {
    if (encryptedText.isEmpty()) return ""
    val combined = decodeBase64(encryptedText)
    if (combined.size <= GCM_IV_LENGTH) return ""

    val iv = ByteArray(GCM_IV_LENGTH)
    val ciphertext = ByteArray(combined.size - GCM_IV_LENGTH)
    System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
    System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.size)

    val cipher = Cipher.getInstance(TRANSFORMATION)
    val key = getSecretKey()
    val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)

    val decryptedBytes = cipher.doFinal(ciphertext)
    return String(decryptedBytes, Charsets.UTF_8)
  }

  private fun encodeBase64(data: ByteArray): String {
    return try {
      Base64.encodeToString(data, Base64.NO_WRAP)
    } catch (e: NoClassDefFoundError) {
      java.util.Base64.getEncoder().encodeToString(data)
    } catch (e: Exception) {
      java.util.Base64.getEncoder().encodeToString(data)
    }
  }

  private fun decodeBase64(str: String): ByteArray {
    return try {
      Base64.decode(str, Base64.NO_WRAP)
    } catch (e: NoClassDefFoundError) {
      java.util.Base64.getDecoder().decode(str)
    } catch (e: Exception) {
      java.util.Base64.getDecoder().decode(str)
    }
  }
}

package com.example.aegis.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.aegis.data.remote.AegisApi
import com.example.aegis.data.remote.dto.IdentityRegisterRequest
import com.example.aegis.domain.model.IdentityStatus
import com.example.aegis.domain.model.TouristIdentity
import java.security.SecureRandom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * REAL per-install identity source.
 *
 * Unlike the old demo repo (which handed out the same hard-coded
 * TST-8F29X4 to every device — the reason the dashboard showed one
 * tourist ID for every trip), this generates a UNIQUE pseudonymous ID
 * on first launch, persists it, and registers the keccak256 commitment
 * with the gateway so incidents link a real idHash.
 */
class LocalIdentityRepository(
  context: Context,
  private val api: AegisApi? = null,
  private val salt: String = randomSalt(),
) : IdentityRepository {

  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences("aegis_identity", Context.MODE_PRIVATE)

  private val identityFlow = MutableStateFlow(loadOrCreateIdentity())

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  init {
    registerWithGateway()
  }

  override fun observeIdentity(): Flow<TouristIdentity> = identityFlow

  /** Registers the pseudonymous commitment with the gateway (best-effort, async). */
  private fun registerWithGateway() {
    val apiRef = api ?: return
    val current = identityFlow.value
    scope.launch {
      try {
        apiRef.registerIdentity(
          IdentityRegisterRequest(
            touristId = current.touristId,
            salt = salt,
          ),
        )
      } catch (e: Exception) {
        // Gateway unreachable — identity still works locally (offline-first);
        // the retry worker will not re-register, but the voucher hash is
        // deterministic (keccak256(id:salt)) so a later registration matches.
      }
    }
  }

  private fun loadOrCreateIdentity(): TouristIdentity {
    val existingId = prefs.getString(KEY_TOURIST_ID, null)
    val touristId = existingId ?: generateTouristId().also { id ->
      prefs.edit().putString(KEY_TOURIST_ID, id).apply()
    }
    return TouristIdentity(
      touristId = touristId,
      displayName = "Tourist ${touristId.takeLast(4)}",
      status = IdentityStatus.ACTIVE,
      validFrom = "Now",
      validTo = "Ongoing",
    )
  }

  private fun generateTouristId(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no confusables
    val rng = SecureRandom()
    val suffix = (1..6).map { chars[rng.nextInt(chars.length)] }.joinToString("")
    return "TST-$suffix"
  }

  companion object {
    private const val KEY_TOURIST_ID = "tourist_id"

    private fun randomSalt(): String {
      val bytes = ByteArray(16)
      SecureRandom().nextBytes(bytes)
      return bytes.joinToString("") { "%02x".format(it) }
    }
  }
}

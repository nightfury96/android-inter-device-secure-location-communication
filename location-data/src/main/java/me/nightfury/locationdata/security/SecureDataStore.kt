package me.nightfury.locationdata.security

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.nightfury.locationdomain.repo.SecureStorage
import java.security.SecureRandom

private val Context.dataStore by preferencesDataStore("secure_prefs")

class SecureDataStore(
    private val context: Context,
    private val aead: Aead
) : SecureStorage {

    private val SERVICE_STATUS = stringPreferencesKey("service_status")
    private val ENCRYPTED_PASS_KEY = stringPreferencesKey("encrypted_db_pass")

    override suspend fun setServiceStatus(isRunning: Boolean) {
        val plain = if (isRunning) "true" else "false"
        val cipherText = aead.encrypt(plain.toByteArray(), null)
        val encoded = Base64.encodeToString(cipherText, Base64.NO_WRAP)
        context.dataStore.edit { prefs ->
            prefs[SERVICE_STATUS] = encoded
        }
    }

    override fun isServiceRunning(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[SERVICE_STATUS]?.let { encoded ->
                try {
                    val decoded = Base64.decode(encoded, Base64.NO_WRAP)
                    val decrypted = aead.decrypt(decoded, null)
                    String(decrypted) == "true"
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            } ?: false
        }
    }

    override suspend fun getOrCreateDbPassphrase(): ByteArray {
        val prefs = context.dataStore.data.first()
        val existingEncrypted = prefs[ENCRYPTED_PASS_KEY]
        return if (existingEncrypted != null) {
            // Decrypt stored key
            val decoded = Base64.decode(existingEncrypted, Base64.NO_WRAP)
            aead.decrypt(decoded, null)
        } else {
            // Generate new random 32-byte passphrase
            val newKey = ByteArray(32).also { SecureRandom().nextBytes(it) }

            // Encrypt with Tink AEAD and store
            val cipherText = aead.encrypt(newKey, null)
            val encoded = Base64.encodeToString(cipherText, Base64.NO_WRAP)
            context.dataStore.edit { prefsMap ->
                prefsMap[ENCRYPTED_PASS_KEY] = encoded
            }
            newKey
        }
    }
}
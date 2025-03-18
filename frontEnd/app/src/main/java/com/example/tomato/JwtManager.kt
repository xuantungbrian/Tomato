import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException

object JwtManager {
    private const val PREFS_NAME = "secure_prefs"
    private const val JWT_KEY = "jwt_token"

    fun saveToken(context: Context, token: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREFS_NAME, masterKeyAlias, context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putString(JWT_KEY, token).apply()
    }

    fun getToken(context: Context): String? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        try {
            val sharedPreferences = EncryptedSharedPreferences.create(
                PREFS_NAME, masterKeyAlias, context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            return sharedPreferences.getString(JWT_KEY, null)
        }
        catch (e: GeneralSecurityException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun clearToken(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREFS_NAME, masterKeyAlias, context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().remove(JWT_KEY).apply()
    }
}

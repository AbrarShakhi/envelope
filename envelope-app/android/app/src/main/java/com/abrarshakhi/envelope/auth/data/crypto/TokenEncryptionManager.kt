package com.abrarshakhi.envelope.auth.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class TokenEncryptionManager(
    private val keyAlias: String = DEFAULT_KEY_ALIAS,
) {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
        load(null)
    }

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        }
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val payload = ByteArray(iv.size + ciphertext.size).apply {
            System.arraycopy(iv, 0, this, 0, iv.size)
            System.arraycopy(ciphertext, 0, this, iv.size, ciphertext.size)
        }
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = payload.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val ciphertext = payload.copyOfRange(GCM_IV_LENGTH_BYTES, payload.size)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
            )
        }
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    fun deleteKey() {
        if (keyStore.containsAlias(keyAlias)) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        keyStore.getKey(keyAlias, null)?.let { return it as SecretKey }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(AES_KEY_SIZE_BITS).setUserAuthenticationRequired(false).build(),
                )
            }.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val DEFAULT_KEY_ALIAS = "auth_token_encryption_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val AES_KEY_SIZE_BITS = 256
        const val GCM_IV_LENGTH_BYTES = 12
        const val GCM_TAG_LENGTH_BITS = 128
    }
}

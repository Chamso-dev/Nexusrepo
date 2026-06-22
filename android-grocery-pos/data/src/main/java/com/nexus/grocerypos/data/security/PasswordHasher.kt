package com.nexus.grocerypos.data.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

/**
 * Salted PBKDF2-HMAC-SHA256 hashing for passwords and PINs. PBKDF2 ships with the
 * platform (API 26+), so the data module stays free of third-party crypto dependencies.
 *
 * Stored format: `iterations:saltHex:hashHex`. The iteration count is embedded so existing
 * hashes keep verifying if the work factor is raised in a future release.
 */
class PasswordHasher @Inject constructor() {

    fun hash(rawValue: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = derive(rawValue, salt, ITERATIONS)
        return "$ITERATIONS:${salt.toHex()}:${hash.toHex()}"
    }

    fun verify(rawValue: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 3) return false
        val iterations = parts[0].toIntOrNull() ?: return false
        val salt = parts[1].fromHexOrNull() ?: return false
        val expected = parts[2].fromHexOrNull() ?: return false
        val actual = derive(rawValue, salt, iterations)
        return constantTimeEquals(actual, expected)
    }

    private fun derive(rawValue: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(rawValue.toCharArray(), salt, iterations, KEY_BITS)
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            return factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHexOrNull(): ByteArray? {
        if (length % 2 != 0) return null
        return runCatching { chunked(2).map { it.toInt(16).toByte() }.toByteArray() }.getOrNull()
    }

    private companion object {
        const val ITERATIONS = 120_000
        const val SALT_BYTES = 16
        const val KEY_BITS = 256
    }
}

package com.nexus.grocerypos.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject

/** PBKDF2-style salted hash, dependency-free so the data module stays lightweight. */
class PasswordHasher @Inject constructor() {

    fun hash(rawValue: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val digest = digest(rawValue, salt)
        return "${salt.toHex()}:${digest.toHex()}"
    }

    fun verify(rawValue: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val salt = parts[0].fromHex()
        val expected = parts[1]
        val actual = digest(rawValue, salt).toHex()
        return constantTimeEquals(actual, expected)
    }

    private fun digest(rawValue: String, salt: ByteArray): ByteArray {
        var result = rawValue.toByteArray(Charsets.UTF_8) + salt
        val sha256 = MessageDigest.getInstance("SHA-256")
        repeat(10_000) {
            result = sha256.digest(result)
        }
        return result
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    private fun String.fromHex(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

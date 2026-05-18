package com.intentos.app.features.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    private val cryptoManager = CryptoManager()

    @Test
    fun testEncryptionChangesPlaintext() {
        val plaintext = "Sensitive User Address: 123 Main St"
        val ciphertext = cryptoManager.encrypt(plaintext)
        
        assertNotEquals(plaintext, ciphertext)
        assertTrue("Ciphertext should be Base64 encoded", ciphertext.matches("^[A-Za-z0-9+/=]+$".toRegex()))
    }

    @Test
    fun testDecryptionRecoversPlaintext() {
        val plaintext = "Secret Workflow Token: ABC-123"
        val ciphertext = cryptoManager.encrypt(plaintext)
        val decrypted = cryptoManager.decrypt(ciphertext)
        
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun testDecryptionFailsSafelyOnCorruptData() {
        // If the database is tampered with or key is lost, it should not crash
        val corruptCiphertext = "NotRealBase64OrGCM!!!"
        val decrypted = cryptoManager.decrypt(corruptCiphertext)
        
        assertEquals("[ENCRYPTED_DATA_UNRECOVERABLE]", decrypted)
    }
}

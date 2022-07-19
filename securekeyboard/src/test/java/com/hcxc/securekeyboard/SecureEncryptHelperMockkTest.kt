package com.hcxc.securekeyboard

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import io.mockk.*
import org.junit.Assert
import org.junit.Test
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class SecureEncryptHelperMockkTest {
    private val aes = "AES/CBC/PKCS7Padding"
    private val store = "AndroidKeyStore"

    @Test
    fun test_encrypt() {
        val cipher = mockk<Cipher>(relaxed = true)
        val helper = spyk<SecureEncryptHelper>()
        val key = mockk<SecretKey>(relaxed = true)
        mockkStatic(Cipher::class)
        mockkStatic(Base64::class)
        every { Cipher.getInstance(aes) } returns cipher
        every { Base64.encodeToString(allAny(), allAny()) } returns ""
        every { helper["generateKey"]() } returns key
        helper.encrypt("input_text")
        verify { cipher.doFinal("input_text".toByteArray()) }
    }

    @Test
    fun test_decrypt() {
        val cipher = mockk<Cipher>(relaxed = true)
        val helper = spyk<SecureEncryptHelper>()
        val key = mockk<SecretKey>(relaxed = true)
        mockkStatic(Cipher::class)
        mockkStatic(Base64::class)
        mockkConstructor(IvParameterSpec::class)
        every { Cipher.getInstance(aes) } returns cipher
        every { Base64.decode("cipher_text", Base64.DEFAULT) } returns "cipher_text".toByteArray()
        every { helper["getKey"]() } returns key
        InternalPlatformDsl.dynamicSet(helper, "i", "".toByteArray())
        helper.decrypt("cipher_text")
        verify { cipher.doFinal("cipher_text".toByteArray()) }
    }

    @Test
    fun test_generateKey() {
        val helper = spyk<SecureEncryptHelper>()
        val generator = mockk<KeyGenerator>(relaxed = true)
        val builder = mockk<KeyGenParameterSpec.Builder>(relaxed = true)
        val spec = mockk<KeyGenParameterSpec>(relaxed = true)
        mockkStatic(KeyGenerator::class)
        mockkConstructor(KeyGenParameterSpec.Builder::class)
        every { KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, store) } returns generator
        every { generator.init(spec) } just Runs
        every { anyConstructed<KeyGenParameterSpec.Builder>().setBlockModes(KeyProperties.BLOCK_MODE_CBC) } returns builder
        every { anyConstructed<KeyGenParameterSpec.Builder>().setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7) } returns builder
        every { anyConstructed<KeyGenParameterSpec.Builder>().setRandomizedEncryptionRequired(false) } returns builder
        every { anyConstructed<KeyGenParameterSpec.Builder>().build() } returns spec
        InternalPlatformDsl.dynamicCall(helper, "generateKey", emptyArray()) { mockk() }
        verify { generator.generateKey() }
    }

    @Test
    fun test_getKey() {
        val helper = spyk<SecureEncryptHelper>()
        val keyStore = mockk<KeyStore>(relaxed = true)
        val entry = mockk<KeyStore.SecretKeyEntry>(relaxed = true)
        val secretKey = mockk<SecretKey>(relaxed = true)
        val alias = InternalPlatformDsl.dynamicGet(helper, "alias") as String
        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance(store) } returns keyStore
        every { keyStore.getEntry(alias, null) } returns entry
        every { entry.secretKey } returns secretKey
        val realKey = InternalPlatformDsl.dynamicCall(helper, "getKey", emptyArray()) { mockk() }
        Assert.assertEquals(secretKey, realKey)
    }

    @Test
    fun test_getKey_nullEntry() {
        val helper = spyk<SecureEncryptHelper>()
        val keyStore = mockk<KeyStore>(relaxed = true)
        val secretKey = mockk<SecretKey>(relaxed = true)
        val alias = InternalPlatformDsl.dynamicGet(helper, "alias") as String
        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance(store) } returns keyStore
        every { keyStore.getEntry(alias, null) } returns null
        every { helper["generateKey"]() } returns secretKey
        val realKey = InternalPlatformDsl.dynamicCall(helper, "getKey", emptyArray()) { mockk() }
        Assert.assertEquals(secretKey, realKey)
    }
}
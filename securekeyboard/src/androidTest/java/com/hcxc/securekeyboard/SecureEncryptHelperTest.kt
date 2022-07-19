package com.hcxc.securekeyboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureEncryptHelperTest {
    @Test
    fun test_encrypt_decrypt() {
        val helper = SecureEncryptHelper()
        val plainText = "2oisjdlkfj03"
        val cipherText = helper.encrypt(plainText)
        val decryptText = helper.decrypt(cipherText)
        Assert.assertEquals(plainText, decryptText)
    }
}
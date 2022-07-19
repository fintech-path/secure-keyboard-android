/**
 *    Copyright 2007-2022 Home Credit Xinchi Consulting Co. Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hcxc.securekeyboard

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class SecureEncryptHelper {
    private val alias = this.toString()
    private val store = "AndroidKeyStore"
    private val aes = "AES/CBC/PKCS7Padding"
    private lateinit var i: ByteArray

    private fun generateKey(): SecretKey {
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, store)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setRandomizedEncryptionRequired(false)
                        .build()
                )
            }.generateKey()
    }

    private fun getKey(): SecretKey {
        return KeyStore.getInstance(store)
            .apply { load(null) }
            .run { getEntry(alias, null) }
            ?.let { (it as KeyStore.SecretKeyEntry).secretKey }
            ?: generateKey()
    }

    fun encrypt(plainText: String): String {
        return Cipher.getInstance(aes)
            .run {
                init(Cipher.ENCRYPT_MODE, generateKey())
                i = iv
                doFinal(plainText.toByteArray())
            }.let {
                Base64.encodeToString(it, Base64.DEFAULT)
            }
    }

    fun decrypt(cipherText: String): String {
        return Base64.decode(cipherText, Base64.DEFAULT)
            .let {
                Cipher.getInstance(aes)
                    .run {
                        init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(i))
                        doFinal(it)
                    }.let {
                        String(it, Charsets.UTF_8)
                    }
            }
    }
}
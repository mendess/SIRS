package sirs.spykid.util

import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionAlgorithm {

    //Acceptable: AES with key size of eat least 128 bits, rather 192 or 256
    private val keySize : Int = 128

    @Throws(Exception::class)
    fun generateSecretKey(): SecretKey? {
        val secureRandom = SecureRandom()
        val keyGenerator = KeyGenerator.getInstance("AES")
        //generate a key with secure random
        keyGenerator.init(keySize, secureRandom)
        return keyGenerator.generateKey()
    }

    fun encrypt(secretKey: SecretKey, message: ByteArray) : ByteArray{
        val data = secretKey.encoded
        val keySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(message)
    }

    fun decrypt(secretKey: SecretKey, secret: ByteArray) : ByteArray{
        val message: ByteArray
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        message = cipher.doFinal(secret)
        return message
    }

}
package sirs.spykid.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.security.Key
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class EncryptionAlgorithm {

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    fun generateSecretKey(keystoreAlias: String): Key {
        val androidKeyStore = KeyStore.getDefaultType() //"AndroidKeyStore"
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)

        if (!keyStore.containsAlias(keystoreAlias)) {
            val keyGenerator: KeyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    keystoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build()
            )
            keyGenerator.generateKey()
        }
        return keyStore.getKey(keystoreAlias, null)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    fun generateSessionKey(keystoreAlias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val keyParam = KeyGenParameterSpec.Builder(
            keystoreAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .build()
        keyGenerator.init(keyParam)
        return keyGenerator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    fun loadSecretKey(keystoreAlias: String, guardianKey: Key): KeyStore {
        val androidKeyStore = KeyStore.getDefaultType() //"AndroidKeyStore"
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)

        if (!keyStore.containsAlias(keystoreAlias)) {
            val secretEntry = KeyStore.SecretKeyEntry(guardianKey as SecretKey)
            keyStore.setEntry(keystoreAlias, secretEntry, null)
        }
        return keyStore
    }

    @Throws(Exception::class)
    fun getKey(keystoreAlias: String): Key {
        val androidKeyStore = KeyStore.getDefaultType() //"AndroidKeyStore"
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        return keyStore.getKey(keystoreAlias, null)
    }

    fun encrypt(key: ByteArray, message: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, 0, key.size, "AES")
        val cipher = Cipher.getInstance("AES/OFB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(message)
    }

    fun decrypt(key: ByteArray, secret: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, 0, key.size, "AES")
        val message: ByteArray
        val cipher = Cipher.getInstance("AES/OFB/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        message = cipher.doFinal(secret)
        return message
    }
}

/**
 * Starts a TCP session with the server, negotiating a shared secret
 */
@RequiresApi(Build.VERSION_CODES.O)
class Session(host: String, port: Int) {
    companion object {
        private lateinit var session: Session
        private val monitor = Object()
        fun <T : Requests.ToJson> request(message: T): String {
            synchronized(monitor) {
                if (!::session.isInitialized) {
                    session = Session("192.168.1.95", 6894)
                }
                return session.request(message.toJson())
            }
        }
    }

    private val connection: Socket = Socket(host, port)
    private val connectionReader: BufferedReader
    private val sessionKey: ByteArray
    private val challenge: ByteArray

    init {
        val (key, challenge) = generateSessionKey(this.connection)
        Log.d("INFO", "[${key.joinToString(" ,")}}]")
        Log.d("INFO", "[${challenge.joinToString(" ,")}]")
        this.sessionKey = key
        this.challenge = challenge
        this.connectionReader = BufferedReader(InputStreamReader(this.connection.getInputStream()))
    }

    /**
     * Sends a request encrypted with the shared secret and the session key
     */
    internal fun request(message: String): String {
        // Encrypt with session key TODO: check if methods from EncryptionAlgorithm class can be used
        val keySpec = SecretKeySpec(this.sessionKey, 0, this.sessionKey.size, "AES")
        val cipher = Cipher.getInstance("AES/OFB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val cipherText =
            String(Base64.getEncoder().encode(cipher.doFinal(message.toByteArray() + this.challenge)))

        // Construct and send packet
        val packet = JsonObject()
        packet.add("payload", JsonPrimitive(cipherText))
        packet.add("iv", JsonPrimitive(Base64.getEncoder().encodeToString(cipher.iv)))
        this.connection.getOutputStream().write(packet.toString().toByteArray())
        val response = Packet.from(this.connectionReader.readLine())
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(response.iv))
        return String(cipher.doFinal(response.payload))
    }

    private fun generateSessionKey(socket: Socket): Pair<ByteArray, ByteArray> {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val keyParam1 = KeyGenParameterSpec.Builder(
            "sessionKey",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        val keyParam2 = keyParam1.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        val keyParam3 = keyParam2.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        val keyParam4 = keyParam3.setRandomizedEncryptionRequired(false)
        val keyParam = keyParam4.build()
        keyGenerator.init(keyParam)
        val key = ByteArray(32)
        Random().nextBytes(key)
        socket.getOutputStream().write(key)
        val challenge = ByteArray(32)
        socket.getInputStream().read(challenge, 0, 32)
        return Pair(key, challenge)
    }

    private class Packet private constructor(val iv: ByteArray, val payload: ByteArray) {
        companion object {
            internal fun from(data: String): Packet {
                val jsonObj = JsonParser.parseString(data).asJsonObject
                return Packet(
                    Base64.getDecoder().decode(jsonObj.get("iv").asString),
                    Base64.getDecoder().decode(jsonObj.get("payload").asString)
                )
            }
        }
    }
}

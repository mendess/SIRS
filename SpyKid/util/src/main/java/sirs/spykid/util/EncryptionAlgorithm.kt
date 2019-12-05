package sirs.spykid.util

import android.os.Build
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.*
import java.net.Socket
import java.util.*


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
                    session = Session("localhost", 6894)
                }
                return session.request(message.toJson())
            }
        }
    }

    private val connection: Socket = Socket(host, port)
    private val connectionReader: BufferedReader
    private val sessionKey: ByteArray

    init {
        this.sessionKey = generateSessionKey(this.connection)
        this.connectionReader = BufferedReader(InputStreamReader(this.connection.getInputStream()))
    }

    /**
     * Sends a request encrypted with the shared secret and the session key
     */
    internal fun request(message: String): String {
        // Encrypt message with shared secret
        val encryptionAlgorithm = EncryptionAlgorithm()
        val sharedSecret = encryptionAlgorithm.getKey("SharedSecret")
        val encryptedMessage = encryptionAlgorithm.encrypt(sharedSecret.encoded, message.toByteArray())

        // Encrypt with session key TODO: check if methods from EncryptionAlgorithm class can be used
        val keySpec = SecretKeySpec(this.sessionKey, 0, this.sessionKey.size, "AES")
        val cipher = Cipher.getInstance("AES/OFB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val cipherText = String(Base64.getEncoder().encode(cipher.doFinal(encryptedMessage)))

        // Construct and send packet
        val packet = JsonObject()
        packet.add("payload", JsonPrimitive(cipherText))
        packet.add("iv", JsonPrimitive(Base64.getEncoder().encodeToString(cipher.iv)))
        this.connection.getOutputStream().write(packet.toString().toByteArray())
        val response = Packet.from(this.connectionReader.readLine())
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(response.iv))
        return String(cipher.doFinal(response.payload))
    }

    private fun generateSessionKey(socket: Socket): ByteArray {
        val encryptionAlgorithm = EncryptionAlgorithm()
        val key = encryptionAlgorithm.generateSecretKey("SessionKey")
        return key.encoded
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

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
import org.whispersystems.curve25519.Curve25519
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.*
import java.net.Socket
import java.util.*


class EncryptionAlgorithm {

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    fun generateSecretKey(keystoreAlias: String): KeyStore {
        val androidKeyStore = "AndroidKeyStore"
        var keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)

        if (!keyStore.containsAlias(keystoreAlias)) {
            val keyGenerator: KeyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore)
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
        return keyStore
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    fun loadSecretKey(keystoreAlias: String, guardianKey: Key): KeyStore {
        val androidKeyStore = "AndroidKeyStore"
        var keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)

        if (!keyStore.containsAlias(keystoreAlias)) {
            val secretEntry = KeyStore.SecretKeyEntry(guardianKey as SecretKey)
            keyStore.setEntry(keystoreAlias, secretEntry, null)
        }
        return keyStore
    }

    @Throws(Exception::class)
    fun getKey(keyStore: KeyStore, keystoreAlias: String): Key {
        return keyStore.getKey(keystoreAlias, null)
    }

    fun encrypt(secretKey: Key, message: ByteArray): ByteArray {
        val data = secretKey.encoded
        val keySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(message)
    }

    fun decrypt(secretKey: Key, secret: ByteArray): ByteArray {
        val message: ByteArray
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
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
     * Sends a request encrypted with the shared secret
     */
    internal fun request(message: String): String {
        val keySpec = SecretKeySpec(this.sessionKey, 0, this.sessionKey.size, "AES")
        val cipher = Cipher.getInstance("AES/OFB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val cipherText = String(Base64.getEncoder().encode(cipher.doFinal(message.toByteArray())))
        val packet = JsonObject()
        packet.add("payload", JsonPrimitive(cipherText))
        packet.add("iv", JsonPrimitive(Base64.getEncoder().encodeToString(cipher.iv)))
        this.connection.getOutputStream().write(packet.toString().toByteArray())
        val response = Packet.from(this.connectionReader.readLine())
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(response.iv))
        return String(cipher.doFinal(response.payload))
    }

    private fun generateSessionKey(socket: Socket): ByteArray {
        //Generate ephemeral value and y_a
        val keyPair = Curve25519.getInstance(Curve25519.BEST).generateKeyPair()
        //Send y_a
        socket.getOutputStream().write(keyPair.publicKey)
        // Receive y_b
        val serverPublic = ByteArray(32)
        socket.getInputStream().read(serverPublic)
        // Generate shared secret
        val cipher = Curve25519.getInstance(Curve25519.BEST)
        return cipher.calculateAgreement(serverPublic, keyPair.privateKey)
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

package sirs.spykid.util

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private fun encryptWithPK(key: ByteArray): ByteArray {
    /* TODO
    Figure out how to load a vendored public key (the server's)
    Figure out how to encrypt a asymmetric key
     */
    return key
}

private fun makeRandomBytes(): ByteArray {
    val key = ByteArray(32)
    SecureRandom().nextBytes(key)
    return key
}

@RequiresApi(Build.VERSION_CODES.O)
private fun encrypt(key: ByteArray, message: ByteArray): Packet {
    val keySpec = SecretKeySpec(key, 0, key.size, "AES")
    val cipher = Cipher.getInstance("AES/OFB/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
    return Packet(cipher.iv, cipher.doFinal(message))
}

private fun decrypt(key: ByteArray, packet: Packet): ByteArray {
    val keySpec = SecretKeySpec(key, 0, key.size, "AES")
    val cipher = Cipher.getInstance("AES/OFB/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(packet.iv))
    return cipher.doFinal(packet.payload)
}

@RequiresApi(Build.VERSION_CODES.O)
class EncryptionAlgorithm private constructor(private val filesDir: File) {
    constructor(context: AppCompatActivity) : this(context.filesDir)

    fun generateSecretKey(keystoreAlias: String): SharedKey {
        val keyFile = File(filesDir, keystoreAlias)
        return when {
            !keyFile.exists() -> {
                val key = SharedKey(makeRandomBytes())
                storeSecretKey(keyFile, key)
                key
            }
            else -> getKey(keyFile)
        }
    }

    fun storeSecretKey(keystoreAlias: String, key: SharedKey) {
        val keyFile = File(filesDir, keystoreAlias)
        storeSecretKey(keyFile, key)
    }

    private fun storeSecretKey(keyFile: File, key: SharedKey) {
        val keyEncoded = key.encoded
        OutputStreamWriter(keyFile.outputStream()).write(keyEncoded, 0, keyEncoded.length)
    }

    private fun getKey(keyFile: File): SharedKey {
        return SharedKey.decode(InputStreamReader(keyFile.inputStream()).readText())
    }

    fun getKey(keyName: String): SharedKey? {
        val keyFile = File(filesDir, keyName)
        if (!keyFile.exists()) return null
        return getKey(keyFile)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class SharedKey internal constructor(val key: ByteArray) : Parcelable {
    val encoded: String = Base64.getEncoder().encodeToString(key)

    constructor(parcel: Parcel) : this(parcel.createByteArray()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(key)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SharedKey> {
        override fun createFromParcel(parcel: Parcel): SharedKey {
            return SharedKey(parcel)
        }

        override fun newArray(size: Int): Array<SharedKey?> {
            return arrayOfNulls(size)
        }

        fun decode(key: String): SharedKey = SharedKey(Base64.getDecoder().decode(key))
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
            Log.d("INFO", "Requesting: $message")
            synchronized(monitor) {
                if (!::session.isInitialized) {
                    session = Session("89.154.164.162", 6894)
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
        Log.d("INFO", "Encrypting message")
        val packet = encrypt(this.sessionKey, message.toByteArray() + this.challenge)
        Log.d("INFO", "Sending message")
        this.connection.getOutputStream().write(packet.toString().toByteArray())
        Log.d("INFO", "Getting packet from server")
        val response = Packet.from(this.connectionReader.readLine())
        Log.d("INFO", "Decrypting message")
        return String(decrypt(this.sessionKey, response))
    }

    private fun generateSessionKey(socket: Socket): Pair<ByteArray, ByteArray> {
        val sessionKey = encryptWithPK(makeRandomBytes())
        socket.getOutputStream().write(sessionKey)
        val challenge = ByteArray(32)
        socket.getInputStream().read(challenge, 0, 32)
        return Pair(sessionKey, challenge)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private class Packet internal constructor(val iv: ByteArray, val payload: ByteArray) {
    companion object {
        internal fun from(data: String): Packet {
            val jsonObj = JsonParser.parseString(data).asJsonObject
            return Packet(
                Base64.getDecoder().decode(jsonObj.get("iv").asString),
                Base64.getDecoder().decode(jsonObj.get("payload").asString)
            )
        }
    }

    override fun toString(): String {
        val packet = JsonObject()
        packet.add("payload", JsonPrimitive(Base64.getEncoder().encodeToString(payload)))
        packet.add("iv", JsonPrimitive(Base64.getEncoder().encodeToString(iv)))
        return packet.toString()
    }
}

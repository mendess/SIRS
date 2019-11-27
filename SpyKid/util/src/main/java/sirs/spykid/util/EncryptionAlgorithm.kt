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
import org.whispersystems.curve25519.Curve25519
import java.security.*
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import java.io.*


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

    fun encrypt(secretKey: Key, message: ByteArray) : ByteArray{
        val data = secretKey.encoded
        val keySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(message)
    }

    fun decrypt(secretKey: Key, secret: ByteArray) : ByteArray{
        val message: ByteArray
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        message = cipher.doFinal(secret)
        return message
    }

    fun generateSessionKey(): ByteArray? {
        // Open SSLSocket
        val socket: SSLSocket = SSLSocketFactory.getDefault().run {
            createSocket("http://localhost/", 443) as SSLSocket
        }
        val session = socket.session

        //Generate ephemeral value and y_a
        val keyPair = Curve25519.getInstance(Curve25519.BEST).generateKeyPair()

        //Encode and send y_a
        socket.startHandshake()

        val out = PrintWriter(
            BufferedWriter(
                OutputStreamWriter(
                    socket.outputStream
                )
            )
        )

        out.println(keyPair.publicKey)
        out.println()
        out.flush()

        //Receive y_b
        val incoming = BufferedReader(
            InputStreamReader(
                socket.inputStream
            )
        )

        val inputLine: String = incoming.readLine()
        val bobPubKey = inputLine.toByteArray()

        out.close()
        incoming.close()

        //Create session key
        val cipher = Curve25519.getInstance(Curve25519.BEST)
        val sharedSecret = cipher.calculateAgreement(bobPubKey, keyPair.privateKey)

        socket.close()

        return sharedSecret
    }

}

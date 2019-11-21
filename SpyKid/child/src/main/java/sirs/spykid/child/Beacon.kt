package sirs.spykid.child

import android.location.Location
import android.os.AsyncTask
import sirs.spykid.util.EncryptionAlgorithm
import java.io.*
import java.net.URL
import javax.crypto.SecretKey

class Beacon : AsyncTask<Pair<Location, SecretKey>, Unit, Unit>() {

    private lateinit var mLocation : Location
    private lateinit var secretKey : SecretKey
    private val encryptionAlgorithm = EncryptionAlgorithm()

    fun register() {
        val url = URL("http://localhost:8000/child/create")
        println("opening connection")
        val connection = url.openConnection()
        connection.doOutput = true
        println("Writing json")
        connection.getOutputStream().write("{ \"guardian\": 0 }".toByteArray())
        connection.connect()
        println(BufferedInputStream(connection.getInputStream()).reader().readText())
    }

    fun beacon() {
        while (true) {
            val url = URL("http://localhost:8000/child")
            println("opening connection")
            val connection = url.openConnection()
            connection.doOutput = true
            val locationMessage = ("{ \"child\": 0, \"location\": " + getLocation() + "}").toByteArray()
            val encryptedMessage = encryptionAlgorithm.encrypt(secretKey, locationMessage)
            connection.getOutputStream().write(encryptedMessage)
            connection.connect()
            println(BufferedInputStream(connection.getInputStream()).reader().readText())
            Thread.sleep(1000)
        }
    }

    // TODO: put in right format for database
    private fun getLocation(): Location {
        return mLocation
    }

    fun updateLocation(location: Location) {
        print("Location updated")
        mLocation = location
    }

    override fun doInBackground(vararg params: Pair<Location, SecretKey>?) {
        beacon()
    }

}

class SOS : AsyncTask<SecretKey, Unit, Unit>() {

    private val encryptionAlgorithm = EncryptionAlgorithm()

    private fun sendSOS(secretKey: SecretKey) {
        while (true) {
            val url = URL("http://localhost:8000/child")
            println("opening connection")
            val connection = url.openConnection()
            connection.doOutput = true
            val locationMessage = ("{ \"child\": 0, \"location\": SOS}").toByteArray()
            val encryptedMessage = encryptionAlgorithm.encrypt(secretKey, locationMessage)
            connection.getOutputStream().write(encryptedMessage)
            connection.connect()
            println(BufferedInputStream(connection.getInputStream()).reader().readText())
            Thread.sleep(1000)
        }
    }

    override fun doInBackground(vararg params: SecretKey) {
        sendSOS(params[0])
    }


}
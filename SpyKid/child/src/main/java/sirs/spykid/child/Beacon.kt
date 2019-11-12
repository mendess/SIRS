package sirs.spykid.child

import android.location.Location
import android.os.AsyncTask
import java.io.*
import java.net.URL

class Beacon : AsyncTask<Unit, Unit, Unit>() {

    private lateinit var mLocation : Location

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
            connection.getOutputStream().write("{ \"child\": 0, \"location\": ${getLocation()}}".toByteArray()) //TODO: make sure location is sent properly
            connection.connect()
            println(BufferedInputStream(connection.getInputStream()).reader().readText())
            Thread.sleep(1000)
        }
    }

    private fun getLocation(): Location {
        return mLocation
    }

    fun updateLocation(location: Location) {
        mLocation = location
    }

    override fun doInBackground(vararg params: Unit?) {
        beacon()
    }

}
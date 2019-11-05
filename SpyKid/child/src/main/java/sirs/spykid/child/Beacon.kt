package sirs.spykid.child

import android.os.AsyncTask
import sirs.spykid.util.Location
import java.io.*
import java.net.URL
import kotlin.random.Random

class Beacon : AsyncTask<Unit, Unit, Unit>() {

    private val r = Random(10)

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
            connection.getOutputStream().write("{ \"child\": 0, \"location\": ${getLocation().asJson()}}".toByteArray())
            connection.connect()
            println(BufferedInputStream(connection.getInputStream()).reader().readText())
            Thread.sleep(1000)
        }
    }

    private fun getLocation(): Location {
        return Location(r.nextInt(), r.nextInt()) //TODO: How do we get child's location (GPS)
    }

    override fun doInBackground(vararg params: Unit?) {
        beacon()
    }

}
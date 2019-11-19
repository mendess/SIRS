package sirs.spykid.guardian

import android.os.AsyncTask
import org.json.JSONObject
import java.net.URL

data class GuardianId(val id: Int)
data class ChildId(val id: Int)
data class Child(val id: ChildId, val username: String)

class Location private constructor(val x: Int, val y: Int) {
    companion object { // Static methods
        internal fun fromBytes(data: ByteArray): Location {
            val json = String(data) // TODO: decryption
            val jsonObj = JSONObject(json)
            return Location(jsonObj.optInt("x"), jsonObj.optInt("y"))
        }
    }
}

class ListChildren : AsyncTask<GuardianId, Unit, List<Child>>() {
    companion object {
        internal fun listChildren(guardianId: GuardianId): List<Child> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun doInBackground(vararg params: GuardianId): List<Child> {
        assert(params.isNotEmpty())
        return listChildren(params[0])
    }

}

class GetLocation : AsyncTask<Pair<GuardianId, ChildId>, Unit, Array<Location>>() {
    companion object {
        internal fun getLocation(guardianId: GuardianId, childId: ChildId): Array<Location> =
            arrayOf(
                Location.fromBytes(
                    post(
                        "guardian",
                        makeJson(Pair("child", childId), Pair("guardian", guardianId))
                    )
                )
            )

    }

    override fun doInBackground(vararg params: Pair<GuardianId, ChildId>): Array<Location> {
        assert(params.isNotEmpty())
        return getLocation(params[0].first, params[0].second)
    }

}

class RegisterChild : AsyncTask<Pair<GuardianId, String>, Unit, ChildId>() {
    companion object {
        internal fun registerChild(id: GuardianId, username: String): ChildId {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun doInBackground(vararg params: Pair<GuardianId, String>): ChildId {
        assert(params.isNotEmpty())
        return registerChild(params[0].first, params[0].second)
    }

}

class Register : AsyncTask<Pair<String, String>, Unit, GuardianId>() {
    companion object {
        internal fun register(username: String, password: String): GuardianId = GuardianId(
            String(
                post(
                    "guardian/create",
                    makeJson(Pair("username", username), Pair("password", password))
                )
            ).toInt()
        )
    }

    override fun doInBackground(vararg params: Pair<String, String>): GuardianId {
        assert(params.isNotEmpty())
        return register(params[0].first, params[0].second)
    }

}


internal fun post(endpoint: String, json: String): ByteArray {
    val url = URL("http://localhost:8000/${endpoint}")
    with(url.openConnection()) {
        doOutput = true
        getOutputStream().write(json.toByteArray())
        connect()
        return getInputStream().readBytes()
    }
}

internal fun makeJson(vararg fields: Pair<String, Any>): String =
    "{${fields.joinToString { i -> "\"${i.first}\": \"${i.second}\"" }}}"
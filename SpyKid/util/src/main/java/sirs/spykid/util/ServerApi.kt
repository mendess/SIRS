package sirs.spykid.util

import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.util.*

// ====== Public API ======

data class GuardianId(val id: Int)
data class ChildId(val id: Int)
data class Child(val id: ChildId, val username: String)

class Location(val x: Double, val y: Double) {
    fun encrypt(): ByteArray {
        return "$x:$y".toByteArray()
    }

    companion object { // Static methods
        internal fun fromBytes(data: ByteArray): Location {
            val s = String(data).split(':')
            return Location(s[0].toDouble(), s[1].toDouble())
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
class RegisterGuardian :
    AsyncTask<Pair<String, String>, Unit, Result<Responses.RegisterGuardian, Responses.Error>>() {
    companion object {
        internal fun register(
            username: String,
            password: String
        ): Result<Responses.RegisterGuardian, Responses.Error> =
            resultFromJson(Session.request(Requests.RegisterGuardian(username, password)))
                .map { Responses.RegisterGuardian.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Pair<String, String>): Result<Responses.RegisterGuardian, Responses.Error> {
        assert(params.isNotEmpty())
        return register(
            params[0].first,
            params[0].second
        )
    }

}

@RequiresApi(Build.VERSION_CODES.N)
class ListChildren :
    AsyncTask<GuardianId, Unit, Result<Responses.ListChildren, Responses.Error>>() {
    companion object {
        internal fun listChildren(guardianId: GuardianId): Result<Responses.ListChildren, Responses.Error> =
            resultFromJson(Session.request(Requests.ListChildren(guardianId)))
                .map { Responses.ListChildren.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: GuardianId): Result<Responses.ListChildren, Responses.Error> {
        assert(params.isNotEmpty())
        return listChildren(params[0])
    }

}

@RequiresApi(Build.VERSION_CODES.N)
class ChildLocation :
    AsyncTask<Pair<GuardianId, ChildId>, Unit, Result<Responses.ChildLocation, Responses.Error>>() {
    companion object {
        internal fun getLocation(
            guardianId: GuardianId,
            childId: ChildId
        ): Result<Responses.ChildLocation, Responses.Error> =
            resultFromJson(Session.request(Requests.ChildLocation(guardianId, childId)))
                .map { Responses.ChildLocation.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }

    }

    override fun doInBackground(vararg params: Pair<GuardianId, ChildId>): Result<Responses.ChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        return getLocation(params[0].first, params[0].second)
    }

}

@RequiresApi(Build.VERSION_CODES.N)
class RegisterChild :
    AsyncTask<Pair<GuardianId, String>, Unit, Result<Responses.RegisterChild, Responses.Error>>() {
    companion object {
        internal fun registerChild(
            id: GuardianId,
            username: String
        ): Result<Responses.RegisterChild, Responses.Error> =
            resultFromJson(Session.request(Requests.RegisterChild(id, username)))
                .map { Responses.RegisterChild.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Pair<GuardianId, String>): Result<Responses.RegisterChild, Responses.Error> {
        assert(params.isNotEmpty())
        return registerChild(
            params[0].first,
            params[0].second
        )
    }

}

@RequiresApi(Build.VERSION_CODES.O)
class UpdateChildLocation :
    AsyncTask<Pair<ChildId, Location>, Unit, Result<Responses.UpdateChildLocation, Responses.Error>>() {
    companion object {
        internal fun updateChildLocation(
            child: ChildId,
            location: Location
        ): Result<Responses.UpdateChildLocation, Responses.Error> =
            resultFromJson(Session.request(Requests.UpdateChildLocation(child, location)))
                .map { Responses.UpdateChildLocation.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Pair<ChildId, Location>): Result<Responses.UpdateChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        return updateChildLocation(params[0].first, params[1].second)
    }

}

// ============= Internals ===============

@RequiresApi(Build.VERSION_CODES.O)
class Responses {
    class RegisterGuardian private constructor(val guardianId: GuardianId) {
        companion object {
            fun fromJson(json: JsonElement): RegisterGuardian =
                RegisterGuardian(
                    GuardianId(
                        json.asJsonObject.get("RegisterGuardian").asJsonObject.get(
                            "id"
                        ).asInt
                    )
                )
        }
    }

    class RegisterChild private constructor(val childId: ChildId) {
        companion object {
            fun fromJson(json: JsonElement): RegisterChild =
                RegisterChild(
                    ChildId(
                        json.asJsonObject.get("RegisterChild").asJsonObject.get(
                            "id"
                        ).asInt
                    )
                )
        }
    }

    class ChildLocation private constructor(val locations: List<Location>) {
        companion object {
            fun fromJson(json: JsonElement): ChildLocation =
                ChildLocation(
                    json.asJsonObject.get("ChildLocation").asJsonObject.get(
                        "locations"
                    ).asJsonArray.map { s -> Location.fromBytes(Base64.getDecoder().decode(s.asString)) }
                )
        }
    }

    class ListChildren private constructor(val children: List<Child>) {
        companion object {
            fun fromJson(json: JsonElement): ListChildren =
                ListChildren(
                    json.asJsonObject.get("ListChildren").asJsonObject.get("children").asJsonArray.map { s -> s.asJsonObject }.map { s ->
                        Child(
                            ChildId(s.get("id").asInt),
                            s.get("username").asString
                        )
                    }
                )

        }
    }

    class UpdateChildLocation private constructor() {
        companion object {
            fun fromJson(json: JsonElement): UpdateChildLocation {
                assert(json.asString == "UpdateChildLocation") { json.asString }
                return UpdateChildLocation()
            }
        }
    }

    enum class Error {
        InvalidChild,
        InvalidGuardian,
        NotGuarding,
        AlreadyGuarding,
        InvalidChildOrGuardian,
        CouldntDecodeB64,
        DecryptionFailed,
        InvalidPacketFormat,
        Other,
    }

    companion object {
        internal fun errorFromJson(json: JsonElement): Error = when (json.asString) {
            "InvalidChild" -> Error.InvalidChild
            "InvalidGuardian" -> Error.InvalidGuardian
            "NotGuarding" -> Error.NotGuarding
            "AlreadyGuarding" -> Error.AlreadyGuarding
            "InvalidChildOrGuardian" -> Error.InvalidChildOrGuardian
            "CouldntDecodeB64" -> Error.CouldntDecodeB64
            "DecryptionFailed" -> Error.DecryptionFailed
            "InvalidPacketFormat" -> Error.InvalidPacketFormat
            else -> Error.Other
        }
    }

}

@RequiresApi(Build.VERSION_CODES.N)
private fun resultFromJson(json: String): Result<JsonElement, JsonElement> {
    val jsonObj = JsonParser.parseString(json).asJsonObject
    return when {
        jsonObj.has("Err") -> Err(jsonObj.get("Err"))
        jsonObj.has("Ok") -> Ok(jsonObj.get("Ok"))
        else -> throw RuntimeException("Invalid json")
    }
}

internal class Requests {
    internal interface ToJson {
        fun toJson(): String
    }

    internal class RegisterGuardian(val username: String, val password: String) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("username", JsonPrimitive(username))
            innerJson.add("password", JsonPrimitive(password))
            val json = JsonObject()
            json.add("RegisterGuardian", innerJson)
            return json.toString()
        }
    }

    internal class RegisterChild(val guardian: GuardianId, val username: String) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("guardian", JsonPrimitive(guardian.id))
            innerJson.add("username", JsonPrimitive(username))
            val json = JsonObject()
            json.add("RegisterChild", innerJson)
            return json.toString()
        }
    }

    internal class ChildLocation(val guardian: GuardianId, val child: ChildId) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("guardian", JsonPrimitive(guardian.id))
            innerJson.add("child", JsonPrimitive(child.id))
            val json = JsonObject()
            json.add("ChildLocation", innerJson)
            return json.toString()
        }
    }

    internal class ListChildren(val guardian: GuardianId) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("guardian", JsonPrimitive(guardian.id))
            val json = JsonObject()
            json.add("ListChildren", innerJson)
            return json.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal class UpdateChildLocation(val child: ChildId, val location: Location) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("child", JsonPrimitive(child.id))
            innerJson.add(
                "location",
                JsonPrimitive(Base64.getEncoder().encodeToString(location.encrypt()))
            )
            val json = JsonObject()
            json.add("UpdateChildLocation", innerJson)
            return json.toString()
        }
    }
}


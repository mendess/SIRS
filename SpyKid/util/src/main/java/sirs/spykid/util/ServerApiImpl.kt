package sirs.spykid.util

import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
internal class RegisterGuardian(private val callback: (Result<Responses.RegisterGuardian, Responses.Error>) -> Unit) :
    AsyncTask<Pair<String, String>, Unit, Result<Responses.RegisterGuardian, Responses.Error>>() {
    companion object {
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.RegisterGuardian, Responses.Error> =
            resultFromJson(Session.request(Requests.RegisterGuardian(username, password)))
                .map { Responses.RegisterGuardian.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Pair<String, String>): Result<Responses.RegisterGuardian, Responses.Error> {
        assert(params.isNotEmpty())
        Log.d(Log.INFO.toString(), "Doing in background" + this.javaClass.canonicalName)
        val r = (run(params[0].first, params[0].second))
        callback(r)
        return r
    }
}

@RequiresApi(Build.VERSION_CODES.N)
internal class LoginGuardian(private val callback: (Result<Responses.LoginGuardian, Responses.Error>) -> Unit) :
    AsyncTask<Pair<String, String>, Unit, Result<Responses.LoginGuardian, Responses.Error>>() {
    companion object {
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.LoginGuardian, Responses.Error> {
            return resultFromJson(Session.request(Requests.LoginGuardian(username, password)))
                .map { Responses.LoginGuardian.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
        }
    }

    override fun doInBackground(vararg params: Pair<String, String>): Result<Responses.LoginGuardian, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0].first, params[0].second)
        callback(r)
        return r
    }

}

@RequiresApi(Build.VERSION_CODES.N)
internal class ListChildren(private val callback: (Result<Responses.ListChildren, Responses.Error>) -> Unit) :
    AsyncTask<Unit, Unit, Result<Responses.ListChildren, Responses.Error>>() {
    companion object {
        internal fun run(): Result<Responses.ListChildren, Responses.Error> =
            resultFromJson(Session.request(Requests.ListChildren()))
                .map { Responses.ListChildren.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Unit): Result<Responses.ListChildren, Responses.Error> {
        val r = run()
        callback(r)
        return r
    }

}

@RequiresApi(Build.VERSION_CODES.N)
internal class ChildLocation(private val callback: (Result<Responses.ChildLocation, Responses.Error>) -> Unit) :
    AsyncTask<ChildId, Unit, Result<Responses.ChildLocation, Responses.Error>>() {
    companion object {
        internal fun run(
            childToken: ChildId
        ): Result<Responses.ChildLocation, Responses.Error> =
            resultFromJson(Session.request(Requests.ChildLocation(childToken)))
                .map { Responses.ChildLocation.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }

    }

    override fun doInBackground(vararg params: ChildId): Result<Responses.ChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0])
        callback(r)
        return r
    }

}

@RequiresApi(Build.VERSION_CODES.N)
internal class RegisterChild(private val callback: (Result<Responses.RegisterChild, Responses.Error>) -> Unit) :
    AsyncTask<Pair<String, String>, Unit, Result<Responses.RegisterChild, Responses.Error>>() {
    companion object {
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.RegisterChild, Responses.Error> =
            resultFromJson(
                Session.request(
                    Requests.RegisterChild(
                        username,
                        password
                    )
                )
            )
                .map { Responses.RegisterChild.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Pair<String, String>): Result<Responses.RegisterChild, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0].first, params[0].second)
        callback(r)
        return r
    }
}

@RequiresApi(Build.VERSION_CODES.N)
internal class LoginChild(private val callback: (Result<Responses.LoginChild, Responses.Error>) -> Unit) :
    AsyncTask<Pair<String, String>, Unit, Result<Responses.LoginChild, Responses.Error>>() {
    companion object {
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.LoginChild, Responses.Error> {
            return resultFromJson(Session.request(Requests.LoginChild(username, password)))
                .map { Responses.LoginChild.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
        }
    }

    override fun doInBackground(vararg params: Pair<String, String>): Result<Responses.LoginChild, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0].first, params[0].second)
        callback(r)
        return r
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal class UpdateChildLocation(private val callback: (Result<Responses.UpdateChildLocation, Responses.Error>) -> Unit) :
    AsyncTask<Location, Unit, Result<Responses.UpdateChildLocation, Responses.Error>>() {
    companion object {
        internal fun run(
            location: Location
        ): Result<Responses.UpdateChildLocation, Responses.Error> =
            resultFromJson(Session.request(Requests.UpdateChildLocation(location)))
                .map { Responses.UpdateChildLocation.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Location): Result<Responses.UpdateChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0])
        callback(r)
        return r
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class Responses {
    class RegisterGuardian private constructor() {
        companion object {
            internal fun fromJson(json: JsonElement): RegisterGuardian {
                assert(json.asString == "Success") { json.asString }
                return RegisterGuardian()
            }
        }
    }

    class LoginGuardian private constructor() {
        companion object {
            internal fun fromJson(json: JsonElement): LoginGuardian {
                assert(json.asString == "Success") { json.asString }
                return LoginGuardian()
            }
        }

    }

    class RegisterChild private constructor(val childId: ChildId) {
        companion object {
            internal fun fromJson(json: JsonElement): RegisterChild =
                RegisterChild(
                    ChildId(
                        json.asJsonObject.get("RegisterChild").asJsonObject.get(
                            "id"
                        ).asInt
                    )
                )
        }
    }

    class LoginChild private constructor() {
        companion object {
            internal fun fromJson(json: JsonElement): LoginChild {
                assert(json.asString == "Success") { json.asString }
                return LoginChild()
            }
        }
    }

    class ChildLocation private constructor(val locations: List<Location>) {
        companion object {
            internal fun fromJson(json: JsonElement): ChildLocation =
                ChildLocation(
                    json.asJsonObject.get("ChildLocation").asJsonObject.get("locations").asJsonArray
                        .map { s -> Location.decrypt(Base64.getDecoder().decode(s.asString)) }
                        .sortedByDescending { it.timestamp }
                )
        }
    }

    class ListChildren private constructor(val children: List<Child>) {
        companion object {
            internal fun fromJson(json: JsonElement): ListChildren =
                ListChildren(
                    json.asJsonObject.get("ListChildren").asJsonObject.get("children").asJsonArray.map { it.asJsonObject }.map {
                        Child(
                            ChildId(it.get("id").asInt),
                            it.get("username").asString
                        )
                    }
                )

        }
    }

    class UpdateChildLocation private constructor() {
        companion object {
            internal fun fromJson(json: JsonElement): UpdateChildLocation {
                assert(json.asString == "Success") { json.asString }
                return UpdateChildLocation()
            }
        }
    }

    enum class Error {
        AlreadyGuarding,
        CouldntDecodeB64,
        DecryptionFailed,
        InvalidChallenge,
        InvalidChild,
        InvalidChildOrGuardian,
        InvalidGuardian,
        InvalidPacketFormat,
        NotGuarding,
        NotLoggedIn,
        Other
    }

    companion object {
        internal fun errorFromJson(json: JsonElement): Error = when (json.asString) {
            "AlreadyGuarding" -> Error.AlreadyGuarding
            "CouldntDecodeB64" -> Error.CouldntDecodeB64
            "DecryptionFailed" -> Error.DecryptionFailed
            "InvalidChallenge" -> Error.InvalidChallenge
            "InvalidChild" -> Error.InvalidChild
            "InvalidChildOrGuardian" -> Error.InvalidChildOrGuardian
            "InvalidGuardian" -> Error.InvalidGuardian
            "InvalidPacketFormat" -> Error.InvalidPacketFormat
            "NotGuarding" -> Error.NotGuarding
            "NotLoggedIn" -> Error.NotLoggedIn
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

    internal class RegisterGuardian(private val username: String, private val password: String) :
        ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("username", JsonPrimitive(username))
            innerJson.add("password", JsonPrimitive(password))
            val json = JsonObject()
            json.add("RegisterGuardian", innerJson)
            return json.toString()
        }
    }

    class LoginGuardian(private val username: String, private val password: String) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("username", JsonPrimitive(username))
            innerJson.add("password", JsonPrimitive(password))
            val json = JsonObject()
            json.add("LoginGuardian", innerJson)
            return json.toString()
        }
    }

    internal class RegisterChild(
        private val username: String,
        private val password: String
    ) :
        ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("username", JsonPrimitive(username))
            innerJson.add("password", JsonPrimitive(password))
            val json = JsonObject()
            json.add("RegisterChild", innerJson)
            return json.toString()
        }
    }

    internal class LoginChild(private val username: String, private val password: String) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("username", JsonPrimitive(username))
            innerJson.add("password", JsonPrimitive(password))
            val json = JsonObject()
            json.add("LoginChild", innerJson)
            return json.toString()
        }

    }

    internal class ChildLocation(private val child: ChildId) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("child", JsonPrimitive(child.id))
            val json = JsonObject()
            json.add("ChildLocation", innerJson)
            return json.toString()
        }
    }

    internal class ListChildren : ToJson {
        override fun toJson(): String {
            return "\"ListChildren\""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal class UpdateChildLocation(
        private val location: Location
    ) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
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


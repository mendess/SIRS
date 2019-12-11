package sirs.spykid.util

import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

@RequiresApi(Build.VERSION_CODES.N)
internal class RegisterGuardian(private val callback: (Result<Responses.RegisterGuardian, Responses.Error>) -> Unit) :
    AsyncTask<Pair<String, String>, Unit, Result<Responses.RegisterGuardian, Responses.Error>>() {
    companion object {
        @ExperimentalStdlibApi
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.RegisterGuardian, Responses.Error> {
            val hashedPassword = getSecurePassword(username, password)
            return resultFromJson(Session.request(Requests.RegisterGuardian(username, hashedPassword))!!)
                .map { Responses.RegisterGuardian.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
        }
    }

    @ExperimentalStdlibApi
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
        @ExperimentalStdlibApi
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.LoginGuardian, Responses.Error> {
            val hashedPassword = getSecurePassword(username, password)
            return resultFromJson(Session.request(Requests.LoginGuardian(username, hashedPassword))!!)
                .map { Responses.LoginGuardian.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
        }
    }

    @ExperimentalStdlibApi
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
            resultFromJson(Session.request(Requests.ListChildren())!!)
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
    AsyncTask<Pair<ChildId, String>, Unit, Result<Responses.ChildLocation, Responses.Error>>() {
    companion object {
        internal fun run(
            childToken: ChildId,
            childName: String
        ): Result<Responses.ChildLocation, Responses.Error> =
            resultFromJson(Session.request(Requests.ChildLocation(childToken))!!)
                .map { Responses.ChildLocation.fromJson(it, childName) }
                .mapErr { Responses.errorFromJson(it) }

    }

    override fun doInBackground(vararg params: Pair<ChildId, String>): Result<Responses.ChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0].first, params[0].second)
        callback(r)
        return r
    }

}

@RequiresApi(Build.VERSION_CODES.N)
internal class RegisterChild(private val callback: (Result<Responses.RegisterChild, Responses.Error>) -> Unit) :
    AsyncTask<Pair<String, String>, Unit, Result<Responses.RegisterChild, Responses.Error>>() {
    companion object {
        @ExperimentalStdlibApi
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.RegisterChild, Responses.Error> {
            val hashedPassword = getSecurePassword(username, password)
            return resultFromJson(Session.request(Requests.RegisterChild(username, hashedPassword))!!)
                .map { Responses.RegisterChild.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
        }
    }

    @ExperimentalStdlibApi
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
        @ExperimentalStdlibApi
        internal fun run(
            username: String,
            password: String
        ): Result<Responses.LoginChild, Responses.Error> {
            val hashedPassword = getSecurePassword(username, password)
            return resultFromJson(Session.request(Requests.LoginChild(username, hashedPassword))!!)
                .map { Responses.LoginChild.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
        }
    }

    @ExperimentalStdlibApi
    override fun doInBackground(vararg params: Pair<String, String>): Result<Responses.LoginChild, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0].first, params[0].second)
        callback(r)
        return r
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal class UpdateChildLocation(private val callback: (Result<Responses.UpdateChildLocation, Responses.Error>) -> Unit) :
    AsyncTask<Pair<Location, String>, Unit, Result<Responses.UpdateChildLocation, Responses.Error>>() {
    companion object {
        internal fun run(
            location: Location,
            username: String
        ): Result<Responses.UpdateChildLocation, Responses.Error> =
            Session.request(Requests.UpdateChildLocation(location, username))?.let { r ->
                resultFromJson(r)
                    .map { Responses.UpdateChildLocation.fromJson(it) }
                    .mapErr { Responses.errorFromJson(it) }
            } ?: Err(Responses.noSecretKey())
    }

    override fun doInBackground(vararg params: Pair<Location, String>): Result<Responses.UpdateChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        val r = run(params[0].first, params[0].second)
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
            internal fun fromJson(json: JsonElement, childName: String): ChildLocation =
                ChildLocation(
                    json.asJsonObject.get("ChildLocation").asJsonObject.get("locations").asJsonArray
                        .mapNotNull { Location.decrypt(it.asString, childName) }
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
        InvalidUsernameOrPassword,
        NotGuarding,
        NotLoggedIn,
        Other,
        UsernameTaken,

        NoSecretKeySet,
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
            "UsernameTaken" -> Error.UsernameTaken
            "InvalidUsernameOrPassword" -> Error.InvalidUsernameOrPassword
            else -> Error.Other
        }

        internal fun noSecretKey(): Error = Error.NoSecretKeySet
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
        fun toJson(): String?
    }

    internal class RegisterGuardian(private val username: String, private val password: String) :
        ToJson {
        override fun toJson(): String =
            JsonObject().run {
                add("RegisterGuardian", JsonObject().apply {
                    add("username", JsonPrimitive(username))
                    add("password", JsonPrimitive(password))
                })
                toString()
            }
    }

    class LoginGuardian(private val username: String, private val password: String) : ToJson {
        override fun toJson(): String =
            JsonObject().run {
                add("LoginGuardian", JsonObject().apply {
                    add("username", JsonPrimitive(username))
                    add("password", JsonPrimitive(password))
                })
                toString()
            }
    }

    internal class RegisterChild(
        private val username: String,
        private val password: String
    ) :
        ToJson {
        override fun toJson(): String =
            JsonObject().run {
                add("RegisterChild", JsonObject().apply {
                    add("username", JsonPrimitive(username))
                    add("password", JsonPrimitive(password))
                })
                toString()
            }
    }

    internal class LoginChild(private val username: String, private val password: String) : ToJson {
        override fun toJson(): String =
            JsonObject().run {
                add("LoginChild", JsonObject().apply {
                    add("username", JsonPrimitive(username))
                    add("password", JsonPrimitive(password))
                })
                toString()
            }
    }

    internal class ChildLocation(private val child: ChildId) : ToJson {
        override fun toJson(): String =
            JsonObject().run {
                add("ChildLocation", JsonObject().apply {
                    add("child", JsonPrimitive(child.id))
                })
                toString()
            }
    }

    internal class ListChildren : ToJson {
        override fun toJson(): String = "\"ListChildren\""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal class UpdateChildLocation(private val location: Location, private val username: String) : ToJson {
        override fun toJson(): String? =
            location.encrypt(username)?.toString()?.let { l ->
                JsonObject().run {
                    add("UpdateChildLocation", JsonObject().apply {
                        add("location", JsonPrimitive(l))
                    })
                    toString()
                }
            }
    }

}


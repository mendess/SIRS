package sirs.spykid.util

import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.util.*
import java.util.function.Consumer

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
        return run(
            params[0].first,
            params[0].second
        )
    }
}

@RequiresApi(Build.VERSION_CODES.N)
internal class LoginGuardian(callback: (Result<Responses.LoginGuardian, Responses.Error>) -> Unit):
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
        return run(params[0].first, params[1].second)
    }

}

@RequiresApi(Build.VERSION_CODES.N)
internal class ListChildren :
    AsyncTask<GuardianToken, Unit, Result<Responses.ListChildren, Responses.Error>>() {
    companion object {
        internal fun run(guardianToken: GuardianToken): Result<Responses.ListChildren, Responses.Error> =
            resultFromJson(Session.request(Requests.ListChildren(guardianToken)))
                .map { Responses.ListChildren.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: GuardianToken): Result<Responses.ListChildren, Responses.Error> {
        assert(params.isNotEmpty())
        return run(params[0])
    }

}

@RequiresApi(Build.VERSION_CODES.N)
internal class ChildLocation :
    AsyncTask<Pair<GuardianToken, ChildId>, Unit, Result<Responses.ChildLocation, Responses.Error>>() {
    companion object {
        internal fun run(
            guardianToken: GuardianToken,
            childToken: ChildId
        ): Result<Responses.ChildLocation, Responses.Error> =
            resultFromJson(Session.request(Requests.ChildLocation(guardianToken, childToken)))
                .map { Responses.ChildLocation.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }

    }

    override fun doInBackground(vararg params: Pair<GuardianToken, ChildId>): Result<Responses.ChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        return run(params[0].first, params[0].second)
    }

}

@RequiresApi(Build.VERSION_CODES.N)
internal class RegisterChild :
    AsyncTask<Triple<GuardianToken, String, String>, Unit, Result<Responses.RegisterChild, Responses.Error>>() {
    companion object {
        internal fun run(
            guardianToken: GuardianToken,
            username: String,
            password: String
        ): Result<Responses.RegisterChild, Responses.Error> =
            resultFromJson(Session.request(Requests.RegisterChild(guardianToken, username, password)))
                .map { Responses.RegisterChild.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Triple<GuardianToken, String, String>): Result<Responses.RegisterChild, Responses.Error> {
        assert(params.isNotEmpty())
        return run(
            params[0].first,
            params[0].second,
            params[0].third
        )
    }
}

@RequiresApi(Build.VERSION_CODES.N)
internal class LoginChild :
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
        return run(params[0].first, params[1].second)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal class UpdateChildLocation :
    AsyncTask<Pair<ChildToken, Location>, Unit, Result<Responses.UpdateChildLocation, Responses.Error>>() {
    companion object {
        internal fun run(
            child: ChildToken,
            location: Location
        ): Result<Responses.UpdateChildLocation, Responses.Error> =
            resultFromJson(Session.request(Requests.UpdateChildLocation(child, location)))
                .map { Responses.UpdateChildLocation.fromJson(it) }
                .mapErr { Responses.errorFromJson(it) }
    }

    override fun doInBackground(vararg params: Pair<ChildToken, Location>): Result<Responses.UpdateChildLocation, Responses.Error> {
        assert(params.isNotEmpty())
        return run(params[0].first, params[1].second)
    }

}

@RequiresApi(Build.VERSION_CODES.O)
class Responses {
    class RegisterGuardian private constructor(val guardianToken: GuardianToken) {
        companion object {
            internal fun fromJson(json: JsonElement): RegisterGuardian =
                RegisterGuardian(
                    GuardianToken(
                        json.asJsonObject.get("RegisterGuardian").asJsonObject.get(
                            "id"
                        ).asInt
                    )
                )
        }
    }

    class LoginGuardian private constructor(val guardianToken: GuardianToken) {
        companion object {
            internal fun fromJson(json: JsonElement): LoginGuardian =
                LoginGuardian(
                    GuardianToken(json.asJsonObject.get("LoginGuardian").asJsonObject.get("id").asInt)
                )
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

    class LoginChild private constructor(val childToken: ChildToken) {
        companion object {
            internal fun fromJson(json: JsonElement): LoginChild =
                LoginChild(
                    ChildToken(json.asJsonObject.get("LoginChild").asJsonObject.get("id").asInt)
                )
        }
    }

    class ChildLocation private constructor(val locations: List<Location>) {
        companion object {
            internal fun fromJson(json: JsonElement): ChildLocation =
                ChildLocation(
                    json.asJsonObject.get("ChildLocation").asJsonObject.get(
                        "locations"
                    ).asJsonArray.map { s -> Location.decrypt(Base64.getDecoder().decode(s.asString)) }
                )
        }
    }

    class ListChildren private constructor(val children: List<Child>) {
        companion object {
            internal fun fromJson(json: JsonElement): ListChildren =
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
            internal fun fromJson(json: JsonElement): UpdateChildLocation {
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
        private val guardian: GuardianToken,
        private val username: String,
        private val password: String
    ) :
        ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("guardian", JsonPrimitive(guardian.id))
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

    internal class ChildLocation(private val guardian: GuardianToken, val child: ChildId) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("guardian", JsonPrimitive(guardian.id))
            innerJson.add("child", JsonPrimitive(child.id))
            val json = JsonObject()
            json.add("ChildLocation", innerJson)
            return json.toString()
        }
    }

    internal class ListChildren(private val guardian: GuardianToken) : ToJson {
        override fun toJson(): String {
            val innerJson = JsonObject()
            innerJson.add("guardian", JsonPrimitive(guardian.id))
            val json = JsonObject()
            json.add("ListChildren", innerJson)
            return json.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal class UpdateChildLocation(private val child: ChildToken, private val location: Location) :
        ToJson {
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


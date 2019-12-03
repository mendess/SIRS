package sirs.spykid.util

import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.O)
class Location(val x: Double, val y: Double, val timestamp: LocalDateTime) {
    fun encrypt(): ByteArray {
        return "$x|$y|$timestamp".toByteArray()
    }

    companion object { // Static methods
        internal fun fromBytes(data: ByteArray): Location {
            val s = String(data).split('|')
            return Location(s[0].toDouble(), s[1].toDouble(), LocalDateTime.parse(s[2]))
        }
    }
}

data class GuardianToken(internal val id: Int)
data class ChildToken(internal val id: Int)
data class ChildId(internal val id: Int)
data class Child(val id: ChildId, val username: String)

@RequiresApi(Build.VERSION_CODES.N)
fun registerGuardian(
    callback: Consumer<Result<Responses.RegisterGuardian, Responses.Error>>,
    username: String,
    password: String
): AsyncTask<Pair<String, String>, Unit, Result<Responses.RegisterGuardian, Responses.Error>> =
    RegisterGuardian().execute(Pair(username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun loginGuardian(
    callback: Consumer<Result<Responses.LoginGuardian, Responses.Error>>,
    username: String,
    password: String
): AsyncTask<Pair<String, String>, Unit, Result<Responses.LoginGuardian, Responses.Error>> =
    LoginGuardian().execute(Pair(username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun registerChild(
    callback: Consumer<Result<Responses.RegisterChild, Responses.Error>>,
    guardianToken: GuardianToken,
    username: String,
    password: String
): AsyncTask<Triple<GuardianToken, String, String>, Unit, Result<Responses.RegisterChild, Responses.Error>> =
    RegisterChild().execute(Triple(guardianToken, username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun loginChild(
    callback: Consumer<Result<Responses.LoginChild, Responses.Error>>,
    username: String,
    password: String
): AsyncTask<Pair<String, String>, Unit, Result<Responses.LoginChild, Responses.Error>> =
    LoginChild().execute(Pair(username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun listChildren(
    callback: Consumer<Result<Responses.ListChildren, Responses.Error>>,
    guardianToken: GuardianToken
): AsyncTask<GuardianToken, Unit, Result<Responses.ListChildren, Responses.Error>> =
    ListChildren().execute(guardianToken)

@RequiresApi(Build.VERSION_CODES.N)
fun childLocation(
    callback: Consumer<Result<Responses.ChildLocation, Responses.Error>>,
    guardianToken: GuardianToken,
    childToken: ChildId
) {
    ChildLocation().execute(Pair(guardianToken, childToken))
}

@RequiresApi(Build.VERSION_CODES.O)
fun updateChildLocation(
    callback: Consumer<Result<Responses.UpdateChildLocation, Responses.Error>>,
    childToken: ChildToken,
    location: Location
) {
    UpdateChildLocation().execute(Pair(childToken, location))
}
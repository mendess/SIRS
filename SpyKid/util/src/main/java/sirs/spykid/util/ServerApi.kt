package sirs.spykid.util

import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

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
    username: String,
    password: String
): AsyncTask<Pair<String, String>, Unit, Result<Responses.RegisterGuardian, Responses.Error>> =
    RegisterGuardian().execute(Pair(username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun loginGuardian(
    username: String,
    password: String
): AsyncTask<Pair<String, String>, Unit, Result<Responses.LoginGuardian, Responses.Error>> =
    LoginGuardian().execute(Pair(username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun registerChild(
    guardianToken: GuardianToken,
    username: String,
    password: String
): AsyncTask<Triple<GuardianToken, String, String>, Unit, Result<Responses.RegisterChild, Responses.Error>> =
    RegisterChild().execute(Triple(guardianToken, username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun loginChild(
    username: String,
    password: String
): AsyncTask<Pair<String, String>, Unit, Result<Responses.LoginChild, Responses.Error>> =
    LoginChild().execute(Pair(username, password))

@RequiresApi(Build.VERSION_CODES.N)
fun listChildren(
    guardianToken: GuardianToken
): AsyncTask<GuardianToken, Unit, Result<Responses.ListChildren, Responses.Error>> =
    ListChildren().execute(guardianToken)

@RequiresApi(Build.VERSION_CODES.N)
fun childLocation(
    guardianToken: GuardianToken,
    childToken: ChildId
): AsyncTask<Pair<GuardianToken, ChildId>, Unit, Result<Responses.ChildLocation, Responses.Error>> =
    ChildLocation().execute(Pair(guardianToken, childToken))

@RequiresApi(Build.VERSION_CODES.O)
fun updateChildLocation(
    childToken: ChildToken,
    location: Location
): AsyncTask<Pair<ChildToken, Location>, Unit, Result<Responses.UpdateChildLocation, Responses.Error>> =
    UpdateChildLocation().execute(Pair(childToken, location))
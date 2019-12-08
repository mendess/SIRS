package sirs.spykid.util

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.O)
data class Location(val x: Double, val y: Double, val timestamp: LocalDateTime) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        LocalDateTime.parse(parcel.readString())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(x)
        parcel.writeDouble(y)
        parcel.writeString(timestamp.toString())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Location> {
        override fun createFromParcel(parcel: Parcel): Location {
            return Location(parcel)
        }

        override fun newArray(size: Int): Array<Location?> {
            return arrayOfNulls(size)
        }

        internal fun decrypt(data: ByteArray): Location {
            val s = String(data).split('|')
            return Location(s[0].toDouble(), s[1].toDouble(), LocalDateTime.parse(s[2]))
        }
    }

    internal fun encrypt(): ByteArray {
        return "$x|$y|$timestamp".toByteArray()
    }

}

data class GuardianToken(internal val id: Int) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GuardianToken> {
        override fun createFromParcel(parcel: Parcel): GuardianToken {
            return GuardianToken(parcel)
        }

        override fun newArray(size: Int): Array<GuardianToken?> {
            return arrayOfNulls(size)
        }
    }
}

data class ChildToken(internal val id: Int) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChildToken> {
        override fun createFromParcel(parcel: Parcel): ChildToken {
            return ChildToken(parcel)
        }

        override fun newArray(size: Int): Array<ChildToken?> {
            return arrayOfNulls(size)
        }
    }
}

data class ChildId(internal val id: Int) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChildId> {
        override fun createFromParcel(parcel: Parcel): ChildId {
            return ChildId(parcel)
        }

        override fun newArray(size: Int): Array<ChildId?> {
            return arrayOfNulls(size)
        }
    }
}

data class Child internal constructor(val id: ChildId, val username: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable<ChildId>(ChildId::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(id, flags)
        parcel.writeString(username)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Child> {
        override fun createFromParcel(parcel: Parcel): Child {
            return Child(parcel)
        }

        override fun newArray(size: Int): Array<Child?> {
            return arrayOfNulls(size)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun registerGuardian(
    username: String,
    password: String,
    callback: Consumer<Result<Responses.RegisterGuardian, Responses.Error>>
) {
    RegisterGuardian(callback::accept).execute(Pair(username, password))
}

@RequiresApi(Build.VERSION_CODES.N)
fun loginGuardian(
    username: String,
    password: String,
    callback: Consumer<Result<Responses.LoginGuardian, Responses.Error>>
) {
    LoginGuardian(callback::accept).execute(Pair(username, password))
}

@RequiresApi(Build.VERSION_CODES.N)
fun registerChild(
    guardianToken: GuardianToken,
    username: String,
    password: String,
    callback: Consumer<Result<Responses.RegisterChild, Responses.Error>>
) {
    RegisterChild(callback::accept).execute(Triple(guardianToken, username, password))
}

@RequiresApi(Build.VERSION_CODES.N)
fun loginChild(
    username: String,
    password: String,
    callback: Consumer<Result<Responses.LoginChild, Responses.Error>>
) {
    LoginChild(callback::accept).execute(Pair(username, password))
}

@RequiresApi(Build.VERSION_CODES.N)
fun listChildren(
    guardianToken: GuardianToken,
    callback: Consumer<Result<Responses.ListChildren, Responses.Error>>
) {
    ListChildren(callback::accept).execute(guardianToken)
}

@RequiresApi(Build.VERSION_CODES.N)
fun childLocation(
    guardianToken: GuardianToken,
    childToken: ChildId,
    callback: Consumer<Result<Responses.ChildLocation, Responses.Error>>
) {
    ChildLocation(callback::accept).execute(Pair(guardianToken, childToken))
}

@RequiresApi(Build.VERSION_CODES.O)
fun updateChildLocation(
    childToken: ChildToken,
    location: Location,
    callback: Consumer<Result<Responses.UpdateChildLocation, Responses.Error>>
) {
    UpdateChildLocation(callback::accept).execute(Pair(childToken, location))
}
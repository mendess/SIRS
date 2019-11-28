package sirs.spykid.util

import org.junit.Test
import java.time.LocalDateTime
import java.util.*

val characters = ('a'..'z').plus('A'..'Z').plus('0'..'9')
val rng = Random()
fun generateString(): String {
    return (0..40).map { characters[rng.nextInt(characters.size)] }.joinToString("")
}

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun register() {
        RegisterGuardian.run(generateString(), generateString()).unwrap()
    }

    @Test
    fun login() {
        val username = generateString()
        val password = generateString()
        RegisterGuardian.run(username, password).unwrap()
        LoginGuardian.run(username, password).unwrap()
    }

    @Test
    fun registerChild() {
        val gid = RegisterGuardian.run(generateString(), generateString()).unwrap()
        RegisterChild.run(gid.guardianToken, generateString(), generateString()).unwrap()
    }

    @Test
    fun listChildren() {
        val gid = RegisterGuardian.run(generateString(), generateString()).unwrap()
        val cid =
            RegisterChild.run(gid.guardianToken, generateString(), generateString()).unwrap()
        val l = ListChildren.run(gid.guardianToken).unwrap()
        assert(l.children.any { c -> c.id == cid.childId })
    }

    @Test
    fun getLocation() {
        val gid = RegisterGuardian.run(generateString(), generateString()).unwrap()
        val cid =
            RegisterChild.run(gid.guardianToken, generateString(), generateString()).unwrap()
        ChildLocation.run(gid.guardianToken, cid.childId).unwrap()
    }

    @Test
    fun updateChildLocation() {
        val gid = RegisterGuardian.run(generateString(), generateString()).unwrap()
        val username = generateString()
        val password = generateString()
        val cid = RegisterChild.run(gid.guardianToken, username, password).unwrap()
        val ctk = LoginChild.run(username, password).unwrap()
        UpdateChildLocation.run(
            ctk.childToken,
            Location(2.3, 4.5, LocalDateTime.now())
        ).unwrap()
        val l = ChildLocation.run(gid.guardianToken, cid.childId).unwrap()
        assert(l.locations.any {
            it.x > 2.0 && it.x < 3.0
                    && it.y > 4.0 && it.y < 5.0
                    && LocalDateTime.now().isAfter(it.timestamp)
        })
    }

    @Test
    fun loginChild() {
        val gid = RegisterGuardian.run(generateString(), generateString()).unwrap()
        val username = generateString()
        val password = generateString()
        RegisterChild.run(gid.guardianToken, username, password).unwrap()
        LoginChild.run(username, password).unwrap()
    }
}

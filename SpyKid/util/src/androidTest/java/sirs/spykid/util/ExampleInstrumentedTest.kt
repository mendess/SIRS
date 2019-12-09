package sirs.spykid.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer

private val characters = ('a'..'z').plus('A'..'Z').plus('0'..'9')
private val rng = Random()
private fun generateString(): String {
    return (0..40).map { characters[rng.nextInt(characters.size)] }.joinToString("")
}

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun register() {
        registerGuardian(generateString(), generateString(), Consumer {}).get().unwrap()
    }

    @Test
    fun login() {
        val username = generateString()
        val password = generateString()
        registerGuardian(username, password, Consumer {}).get().unwrap()
        loginGuardian(username, password, Consumer {}).get().unwrap()
    }

    @Test
    fun registerChild() {
        registerGuardian(generateString(), generateString(), Consumer {}).get().unwrap()
        registerChild(generateString(), generateString(), Consumer {}).get().unwrap()
    }

    @Test
    fun listChildren() {
        registerGuardian(generateString(), generateString(), Consumer {}).get().unwrap()
        registerChild(generateString(), generateString(), Consumer {}).get().unwrap()
        val c = listChildren(Consumer {}).get().unwrap()
        assert(c.children.isNotEmpty())
    }

    @Test
    fun getLocation() {
        registerGuardian(generateString(), generateString(), Consumer {}).get().unwrap()
        val cid = registerChild(generateString(), generateString(), Consumer {}).get().unwrap()
        childLocation(cid.childId, Consumer {}).get().unwrap()
    }

    @Test
    fun updateChildLocation() {
        val guardianUsername = generateString()
        val guardianPassword = generateString()
        registerGuardian(guardianUsername, guardianPassword, Consumer {}).get().unwrap()
        val childUsername = generateString()
        val childPassword = generateString()
        val cid = registerChild(childUsername, childPassword, Consumer {}).get().unwrap()
        loginChild(childUsername, childPassword, Consumer {}).get().unwrap()
        updateChildLocation(Location(2.3, 4.5, LocalDateTime.now()), Consumer {}).get().unwrap()
        loginGuardian(guardianUsername, guardianPassword, Consumer {}).get().unwrap()
        val l = childLocation(cid.childId, Consumer {}).get().unwrap()
        assert(l.locations.any {
            it.x > 2.0 && it.x < 3.0
                    && it.y > 4.0 && it.y < 5.0
                    && LocalDateTime.now().isAfter(it.timestamp)
        })
    }

    @Test
    fun loginChild() {
        registerGuardian(generateString(), generateString(), Consumer {}).get().unwrap()
        val username = generateString()
        val password = generateString()
        registerChild(username, password, Consumer {}).get()
        loginChild(username, password, Consumer {}).get()
    }
}

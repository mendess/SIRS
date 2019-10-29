package sirs.spykid.child

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BeaconUnitTest {
    @Test
    fun testBeacon() {
        val b = Beacon()
        b.register()
        b.beacon()
    }
}

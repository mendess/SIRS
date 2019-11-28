package sirs.spykid.util

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun register() {
        RegisterGuardian.register("Eva", "cats").unwrap()
    }

    @Test
    fun login() {
        RegisterGuardian.register("Mendes", "rust").unwrap()
        LoginGuardian.login("Mendes", "rust").unwrap()
    }

    @Test
    fun registerChild() {
        val gid = RegisterGuardian.register("Pedro", "linux").unwrap()
        RegisterChild.registerChild(gid.guardianId, "child1").unwrap()
    }

    @Test
    fun listChildren() {
        val gid = RegisterGuardian.register("Felipe", "java").unwrap()
        val cid = RegisterChild.registerChild(gid.guardianId, "child1").unwrap()
        val l = ListChildren.listChildren(gid.guardianId).unwrap()
        assert(l.children.any { c -> c.id == cid.childId })
    }

    @Test
    fun getLocation() {
        val gid = RegisterGuardian.register("Andre", "sirs").unwrap()
        val cid = RegisterChild.registerChild(gid.guardianId, "child1").unwrap()
        ChildLocation.getLocation(gid.guardianId, cid.childId).unwrap()
    }

    @Test
    fun updateChildLocation() {
        val gid = RegisterGuardian.register("Stalker", "snoop").unwrap()
        val cid = RegisterChild.registerChild(gid.guardianId, "kid1").unwrap()
        UpdateChildLocation.updateChildLocation(cid.childId, Location(2.3, 4.5)).unwrap()
        val l = ChildLocation.getLocation(gid.guardianId, cid.childId).unwrap()
        assert(l.locations.any { it.x > 2.0 && it.x < 3.0 && it.y > 4.0 && it.y < 5.0 })
    }
}

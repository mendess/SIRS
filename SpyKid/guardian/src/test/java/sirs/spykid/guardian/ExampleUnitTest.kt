package sirs.spykid.guardian

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun register() {
        Register.register("Eva", "cats")
    }

    @Test
    fun registerChild() {
        val id = Register.register("Pedro", "linux")
        RegisterChild.registerChild(id, "child1")
    }

    @Test
    fun listChildren() {
        val id = Register.register("Felipe", "java")
        RegisterChild.registerChild(id, "child1")
        assert(ListChildren.listChildren(id).isNotEmpty())
    }

    @Test
    fun getLocation() {
        val id = Register.register("Andre", "sirs")
        RegisterChild.registerChild(id, "child1")
        val child = ListChildren.listChildren(id)
        GetLocation.getLocation(id, child[0].id)
    }

}

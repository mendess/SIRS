package sirs.spykid.util

class Location(val x: Int, val y: Int) {

    fun asJson(): String {
        return "{ \"x\": $x, \"y\": $y }"
    }
}
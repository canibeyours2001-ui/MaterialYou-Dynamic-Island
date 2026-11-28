package fr.angel.dynamicisland.plugins

/** Pure, device-independent selection logic. */
internal object ActivityCycle {
    fun nextIndex(current: Int, size: Int, direction: Int): Int {
        require(size > 0) { "Activity list must not be empty" }
        return Math.floorMod(current + direction, size)
    }
}

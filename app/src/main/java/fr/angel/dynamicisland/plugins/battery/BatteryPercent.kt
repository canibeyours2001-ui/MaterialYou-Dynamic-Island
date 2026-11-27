package fr.angel.dynamicisland.plugins.battery

/** Pure arithmetic to handle malformed battery broadcasts without crashing the overlay. */
internal object BatteryPercent {
    fun calculate(level: Int, scale: Int): Int? {
        if (level < 0 || scale <= 0) return null
        return ((level.toLong() * 100L) / scale).toInt().coerceIn(0, 100)
    }
}

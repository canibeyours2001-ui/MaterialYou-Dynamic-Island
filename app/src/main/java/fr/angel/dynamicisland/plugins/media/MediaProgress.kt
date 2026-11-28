package fr.angel.dynamicisland.plugins.media

/** Valid finite progress for live streams, unknown durations and malformed metadata. */
internal object MediaProgress {
    fun percent(positionMs: Long, durationMs: Long): Float {
        if (durationMs <= 0L) return 0f
        return ((positionMs.toDouble() / durationMs.toDouble()) * 100.0)
            .toFloat()
            .coerceIn(0f, 100f)
    }
}

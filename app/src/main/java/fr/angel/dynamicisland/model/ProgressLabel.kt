package fr.angel.dynamicisland.model

/** Converts only actual Android notification progress metadata into UI labels. */
internal object ProgressLabel {
    fun format(progress: Int, max: Int, indeterminate: Boolean): String =
        when {
            indeterminate -> "In progress"
            max > 0 -> "${((progress.coerceIn(0, max).toLong() * 100L) / max)}%"
            else -> ""
        }
}

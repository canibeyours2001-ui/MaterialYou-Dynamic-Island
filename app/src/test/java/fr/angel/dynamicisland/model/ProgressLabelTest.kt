package fr.angel.dynamicisland.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressLabelTest {
    @Test fun computesValidDownloadProgress() {
        assertEquals("50%", ProgressLabel.format(5, 10, false))
        assertEquals("100%", ProgressLabel.format(200, 100, false))
        assertEquals("0%", ProgressLabel.format(-5, 100, false))
    }

    @Test fun handlesUnknownMaximumAndIndeterminate() {
        assertEquals("", ProgressLabel.format(1, 0, false))
        assertEquals("", ProgressLabel.format(1, -1, false))
        assertEquals("In progress", ProgressLabel.format(1, 0, true))
    }

    @Test fun avoidsIntegerOverflow() {
        assertEquals("100%", ProgressLabel.format(Int.MAX_VALUE, Int.MAX_VALUE, false))
    }
}

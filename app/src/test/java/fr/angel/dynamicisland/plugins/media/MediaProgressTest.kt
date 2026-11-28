package fr.angel.dynamicisland.plugins.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaProgressTest {
    @Test fun unknownDurationDoesNotProduceNaN() {
        assertEquals(0f, MediaProgress.percent(500L, 0L), 0f)
        assertEquals(0f, MediaProgress.percent(500L, -1L), 0f)
    }

    @Test fun normalProgressClampsToValidRange() {
        assertEquals(50f, MediaProgress.percent(500L, 1000L), 0.001f)
        assertEquals(0f, MediaProgress.percent(-50L, 1000L), 0f)
        assertEquals(100f, MediaProgress.percent(2000L, 1000L), 0f)
    }

    @Test fun largeDurationsStayFinite() {
        assertEquals(50f, MediaProgress.percent(Long.MAX_VALUE / 2, Long.MAX_VALUE), 0.001f)
    }
}

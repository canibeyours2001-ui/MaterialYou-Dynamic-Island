package fr.angel.dynamicisland.plugins

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ActivityCycleTest {
    @Test fun wrapsForward() {
        assertEquals(0, ActivityCycle.nextIndex(2, 3, 1))
    }

    @Test fun wrapsBackward() {
        assertEquals(2, ActivityCycle.nextIndex(0, 3, -1))
    }

    @Test fun handlesSingleActivity() {
        assertEquals(0, ActivityCycle.nextIndex(0, 1, 1))
    }

    @Test fun rejectsEmptyLists() {
        assertThrows(IllegalArgumentException::class.java) {
            ActivityCycle.nextIndex(0, 0, 1)
        }
    }
}

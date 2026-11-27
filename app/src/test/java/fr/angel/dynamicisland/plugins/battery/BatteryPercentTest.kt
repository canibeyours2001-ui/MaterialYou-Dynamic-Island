package fr.angel.dynamicisland.plugins.battery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryPercentTest {
    @Test fun calculatesNormalLevel() {
        assertEquals(50, BatteryPercent.calculate(25, 50))
        assertEquals(100, BatteryPercent.calculate(150, 100))
        assertEquals(0, BatteryPercent.calculate(0, 100))
    }

    @Test fun rejectsInvalidMeasurements() {
        assertNull(BatteryPercent.calculate(-1, 100))
        assertNull(BatteryPercent.calculate(30, 0))
        assertNull(BatteryPercent.calculate(30, -1))
    }

    @Test fun doesNotOverflowWhenScaling() {
        assertEquals(100, BatteryPercent.calculate(Int.MAX_VALUE, 100))
    }
}

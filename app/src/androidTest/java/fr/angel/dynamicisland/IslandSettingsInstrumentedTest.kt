package fr.angel.dynamicisland

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.angel.dynamicisland.island.IslandSettings
import fr.angel.dynamicisland.model.AUTO_ALIGN_CUTOUT
import fr.angel.dynamicisland.model.HAPTIC_FEEDBACK
import fr.angel.dynamicisland.model.SETTINGS_KEY
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IslandSettingsInstrumentedTest {
    @Test
    fun behaviorPreferencesSurviveReload() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        val originalHaptic = preferences.getBoolean(HAPTIC_FEEDBACK, true)
        val originalCutout = preferences.getBoolean(AUTO_ALIGN_CUTOUT, true)
        val settings = IslandSettings.instance
        try {
            settings.loadSettings(context)
            settings.hapticFeedback = false
            settings.autoAlignCutout = false
            settings.applySettings(context)
            settings.hapticFeedback = true
            settings.autoAlignCutout = true
            settings.loadSettings(context)
            assertFalse(settings.hapticFeedback)
            assertFalse(settings.autoAlignCutout)

            settings.hapticFeedback = true
            settings.autoAlignCutout = true
            settings.applySettings(context)
            settings.hapticFeedback = false
            settings.autoAlignCutout = false
            settings.loadSettings(context)
            assertTrue(settings.hapticFeedback)
            assertTrue(settings.autoAlignCutout)
        } finally {
            settings.hapticFeedback = originalHaptic
            settings.autoAlignCutout = originalCutout
            settings.applySettings(context)
        }
    }
}

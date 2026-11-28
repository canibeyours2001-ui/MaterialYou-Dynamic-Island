package fr.angel.dynamicisland

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.angel.dynamicisland.model.DISCLOSURE_ACCEPTED
import fr.angel.dynamicisland.model.SETTINGS_KEY
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Validates normal navigation after the user has accepted the disclosure.
 * Does not grant privileged system permissions or change production behavior.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    @Test
    fun acceptedDisclosureOpensMainActivity() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
        val acceptedBefore = preferences.getBoolean(DISCLOSURE_ACCEPTED, false)

        try {
            check(preferences.edit().putBoolean(DISCLOSURE_ACCEPTED, true).commit())
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val activity = instrumentation.startActivitySync(intent)
            try {
                assertTrue("Expected main application, not disclosure", activity is MainActivity)
            } finally {
                activity.finish()
            }
        } finally {
            preferences.edit().putBoolean(DISCLOSURE_ACCEPTED, acceptedBefore).commit()
        }
    }
}

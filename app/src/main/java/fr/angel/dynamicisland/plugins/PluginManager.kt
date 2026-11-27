package fr.angel.dynamicisland.plugins

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fr.angel.dynamicisland.plugins.battery.BatteryPlugin
import fr.angel.dynamicisland.plugins.media.MediaSessionPlugin
import fr.angel.dynamicisland.plugins.notification.NotificationPlugin

/** Registers active plugin listeners once and reconciles toggles on settings changes. */
class PluginManager(private val context: Context, private val host: PluginHost) {
    val allPlugins = listOf(NotificationPlugin(), MediaSessionPlugin(), BatteryPlugin())
    val activePlugins = mutableStateListOf<BasePlugin>()
    private val startedPlugins = mutableSetOf<String>()
    private var selectedPluginId by mutableStateOf<String?>(null)

    /** Currently displayed activity. Earlier plugins in allPlugins have higher priority. */
    val currentPlugin: BasePlugin?
        get() = activePlugins.firstOrNull { it.id == selectedPluginId } ?: activePlugins.firstOrNull()

    /** Swipe horizontally on the collapsed island to switch among simultaneous activities. */
    fun cycleActivity(direction: Int) {
        if (activePlugins.size < 2) return
        val current = activePlugins.indexOf(currentPlugin).coerceAtLeast(0)
        selectedPluginId = activePlugins[ActivityCycle.nextIndex(current, activePlugins.size, direction)].id
    }

    fun initialize() {
        allPlugins.forEach { plugin ->
            plugin.enabled.value = plugin.isPluginEnabled(context)
            when {
                plugin.active && startedPlugins.add(plugin.id) -> {
                    try {
                        plugin.onCreate(host)
                    } catch (_: RuntimeException) {
                        startedPlugins.remove(plugin.id)
                        requestDismiss(plugin)
                        runCatching { plugin.onDestroy() }
                    }
                }
                !plugin.active && startedPlugins.remove(plugin.id) -> {
                    requestDismiss(plugin)
                    plugin.onDestroy()
                }
            }
        }
    }

    fun onDestroy() {
        allPlugins.filter { startedPlugins.remove(it.id) }.forEach { it.onDestroy() }
        activePlugins.clear()
        selectedPluginId = null
    }

    fun requestDisplay(plugin: BasePlugin) {
        if (!startedPlugins.contains(plugin.id)) return
        val priority = allPlugins.indexOfFirst { it.id == plugin.id }
        if (activePlugins.contains(plugin)) {
            val visible = currentPlugin
            if (visible == null || priority < allPlugins.indexOfFirst { it.id == visible.id }) {
                selectedPluginId = plugin.id
            }
            return
        }
        val insertion = activePlugins.indexOfFirst {
            allPlugins.indexOfFirst { item -> item.id == it.id } > priority
        }.let { if (it == -1) activePlugins.size else it }
        // A more important event (e.g., a new call/notification) may preempt media,
        // but a background charging event should not interrupt current content.
        val prior = currentPlugin
        activePlugins.add(insertion, plugin)
        if (prior == null || priority < allPlugins.indexOfFirst { it.id == prior.id }) {
            selectedPluginId = plugin.id
        }
    }

    fun requestDismiss(plugin: BasePlugin) {
        activePlugins.remove(plugin)
        if (selectedPluginId == plugin.id) selectedPluginId = null
    }
}

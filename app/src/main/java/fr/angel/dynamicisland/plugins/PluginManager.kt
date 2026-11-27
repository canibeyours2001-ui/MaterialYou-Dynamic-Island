package fr.angel.dynamicisland.plugins

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import fr.angel.dynamicisland.plugins.battery.BatteryPlugin
import fr.angel.dynamicisland.plugins.media.MediaSessionPlugin
import fr.angel.dynamicisland.plugins.notification.NotificationPlugin

/** Registers active plugin listeners once and reconciles toggles on settings changes. */
class PluginManager(private val context: Context, private val host: PluginHost) {
    val allPlugins = listOf(NotificationPlugin(), MediaSessionPlugin(), BatteryPlugin())
    val activePlugins = mutableStateListOf<BasePlugin>()
    private val startedPlugins = mutableSetOf<String>()

    fun initialize() {
        allPlugins.forEach { plugin ->
            plugin.enabled.value = plugin.isPluginEnabled(context)
            when {
                plugin.active && startedPlugins.add(plugin.id) -> plugin.onCreate(host)
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
    }

    fun requestDisplay(plugin: BasePlugin) {
        if (!startedPlugins.contains(plugin.id) || activePlugins.contains(plugin)) return
        val priority = allPlugins.indexOfFirst { it.id == plugin.id }
        val insertion = activePlugins.indexOfFirst {
            allPlugins.indexOfFirst { item -> item.id == it.id } > priority
        }.let { if (it == -1) activePlugins.size else it }
        activePlugins.add(insertion, plugin)
    }

    fun requestDismiss(plugin: BasePlugin) {
        activePlugins.remove(plugin)
    }
}

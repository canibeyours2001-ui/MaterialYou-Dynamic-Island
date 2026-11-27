package fr.angel.dynamicisland.plugins

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import fr.angel.dynamicisland.model.packageName
import fr.angel.dynamicisland.plugins.battery.BatteryPlugin
import fr.angel.dynamicisland.plugins.media.MediaSessionPlugin
import fr.angel.dynamicisland.plugins.notification.NotificationPlugin

class ExportedPlugins {

	companion object {

		val permissions: SnapshotStateMap<String, PluginPermission> = mutableStateMapOf(
			Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS to object : PluginPermission(
				name = "Notification access",
				description = "Allow Dynamic Island to listen to notifications and display them",
				requestIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
			) { override fun checkPermission(context: Context) : Boolean {
                    val enabled = Settings.Secure.getString(
                        context.contentResolver,
                        "enabled_notification_listeners"
                    ) ?: return false
                    // Match a complete component package, not a substring of another app's name.
                    return enabled.split(':').any { flattened ->
                        android.content.ComponentName.unflattenFromString(flattened)?.packageName ==
                            context.packageName
                    }
				} }
		)

		val plugins = arrayListOf(
			NotificationPlugin(),
			MediaSessionPlugin(),
			BatteryPlugin(),
		)

        fun setupPermissions(context: Context) {
            permissions.forEach { (_, permission) ->
                permission.granted.value = permission.checkPermission(context)
            }
            // Read persisted enablement for the shared instances seen by settings and overlay.
            plugins.forEach { plugin ->
                plugin.enabled.value = plugin.isPluginEnabled(context)
            }
        }

		fun getPlugin(pluginId: String): BasePlugin {
			return plugins.first { it.id == pluginId }
		}
	}
}
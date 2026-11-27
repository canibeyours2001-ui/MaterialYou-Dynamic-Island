package fr.angel.dynamicisland.model.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import fr.angel.dynamicisland.island.IslandSettings
import fr.angel.dynamicisland.model.ACTION_CLOSE
import fr.angel.dynamicisland.model.ACTION_OPEN_CLOSE
import fr.angel.dynamicisland.model.NOTIFICATION_POSTED
import fr.angel.dynamicisland.model.NOTIFICATION_REMOVED

class NotificationService : NotificationListenerService() {
    val notifications = mutableStateListOf<StatusBarNotification>()

    companion object {
        private var instance: NotificationService? = null
        fun getInstance(): NotificationService? = instance
    }

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Notification IDs are not unique across apps; the system key is.
            val posted = notifications.firstOrNull {
                it.key == intent.getStringExtra("key")
            } ?: return
            when (intent.action) {
                ACTION_OPEN_CLOSE -> {
                    // Opening should not silently cancel the user's original notification.
                    try {
                        posted.notification.contentIntent?.send()
                    } catch (_: PendingIntent.CanceledException) { }
                }
                ACTION_CLOSE -> dismiss(posted)
            }
        }
    }

    private fun dismiss(posted: StatusBarNotification) {
        if (posted.isClearable) {
            cancelNotification(posted.key)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ContextCompat.registerReceiver(
            this,
            actionReceiver,
            IntentFilter().apply {
                addAction(ACTION_OPEN_CLOSE)
                addAction(ACTION_CLOSE)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val enabledApps = IslandSettings.instance.enabledApps
        if (enabledApps.isNotEmpty() && sbn.packageName !in enabledApps) return
        if (notification.category in setOf(
                Notification.CATEGORY_SYSTEM,
                Notification.CATEGORY_SERVICE,
                Notification.CATEGORY_TRANSPORT
            )
        ) return
        notifications.removeAll { it.key == sbn.key }
        notifications.add(sbn)
        // Bound retained notification metadata even when a misbehaving app spams updates.
        while (notifications.size > 64) notifications.removeAt(0)
        sendBroadcast(Intent(NOTIFICATION_POSTED).apply {
            setPackage(packageName)
            putExtra("key", sbn.key)
            putExtra("id", sbn.id)
            putExtra("package_name", sbn.packageName)
            putExtra("category", notification.category)
            putExtra("time", sbn.postTime)
            putExtra("title", notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
            putExtra("body", notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        })
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        notifications.removeAll { it.key == sbn.key }
        sendBroadcast(Intent(NOTIFICATION_REMOVED).apply {
            setPackage(packageName)
            putExtra("key", sbn.key)
        })
    }

    override fun onDestroy() {
        unregisterReceiver(actionReceiver)
        notifications.clear()
        instance = null
        super.onDestroy()
    }
}

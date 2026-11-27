package fr.angel.dynamicisland.model.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.*
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams.*
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.*
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import fr.angel.dynamicisland.R
import fr.angel.dynamicisland.island.Island
import fr.angel.dynamicisland.island.IslandState
import fr.angel.dynamicisland.island.IslandViewState
import fr.angel.dynamicisland.model.*
import fr.angel.dynamicisland.plugins.BasePlugin
import fr.angel.dynamicisland.plugins.ExportedPlugins
import fr.angel.dynamicisland.plugins.PluginHost
import fr.angel.dynamicisland.plugins.PluginManager
import fr.angel.dynamicisland.ui.island.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel


class IslandOverlayService : AccessibilityService(), PluginHost {

	private val params = WindowManager.LayoutParams(
		MATCH_PARENT,
		WRAP_CONTENT,
		TYPE_ACCESSIBILITY_OVERLAY,
		FLAG_LAYOUT_IN_SCREEN or FLAG_LAYOUT_NO_LIMITS or FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE,
		PixelFormat.TRANSLUCENT
	).apply {
		gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
	}

	private lateinit var settingsPreferences: SharedPreferences
    private var overlayWindowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var overlayLifecycle: MyLifecycleOwner? = null
    private var activeRecomposer: Recomposer? = null
    private var recompositionScope: CoroutineScope? = null
    private var receiverRegistered = false
    private var cleanedUp = false

	// State of the overlay
	var islandState : IslandState by mutableStateOf(IslandViewState.Closed)
		private set

	// Plugins
	lateinit var pluginManager: PluginManager

	// Theme
	var invertedTheme by mutableStateOf(false)

	companion object {
		private var instance: IslandOverlayService? = null

		fun getInstance(): IslandOverlayService? {
			return instance
		}
	}

	private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			when (intent.action) {
				SETTINGS_CHANGED -> {
					init()
				}
				SETTINGS_THEME_INVERTED -> {
					val settingsPreferences = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
					invertedTheme = settingsPreferences.getBoolean(THEME_INVERTED, false)
				}
				ACTION_SCREEN_ON -> {
					Island.isScreenOn = true
				}
				ACTION_SCREEN_OFF -> {
					Island.isScreenOn = false
				}
			}
		}
	}

	override fun onServiceConnected() {
		super.onServiceConnected()
        cleanedUp = false
		setTheme(R.style.Theme_DynamicIsland)
		instance = this
		settingsPreferences = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

		// Register broadcast receiver
		registerReceiver(mBroadcastReceiver, IntentFilter().apply {
			addAction(SETTINGS_CHANGED)
			addAction(SETTINGS_THEME_INVERTED)
			addAction(ACTION_SCREEN_ON)
			addAction(ACTION_SCREEN_OFF)
		}, RECEIVER_NOT_EXPORTED)
        receiverRegistered = true

		// Setup plugins (check if they are enabled)
		ExportedPlugins.setupPermissions(this)
		pluginManager = PluginManager(this, this)

		// Setup
		init()

		val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayWindowManager = windowManager
		showOverlay(windowManager, params)
	}

	fun init() {
		// Re-check notification permission and reconcile plugins without duplicate receivers.
        ExportedPlugins.setupPermissions(this)
		pluginManager.initialize()

		// Setup inverted theme
		val settingsPreferences = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
		invertedTheme = settingsPreferences.getBoolean(THEME_INVERTED, false)
	}

	@SuppressLint("ClickableViewAccessibility")
	private fun showOverlay(
		windowManager: WindowManager,
		params: WindowManager.LayoutParams
	) {
		val composeView = ComposeView(this)
		// Compose is hosted in a managed overlay window.
		composeView.setContent {
			// Listen for plugin changes
			LaunchedEffect(pluginManager.activePlugins.firstOrNull()) {
				islandState = if (pluginManager.activePlugins.firstOrNull() != null) {
					IslandViewState.Opened
				} else {
					IslandViewState.Closed
				}
				Log.d("OverlayService", "Plugins changed: ${pluginManager.activePlugins}")
			}

			IslandApp(
				islandOverlayService = this,
			)
		}

		// TODO: Find a way to detect when a click is performed outside of the overlay (to close it)
		/*composeView.setOnTouchListener { view: View?, event: MotionEvent ->
			if (event.action == MotionEvent.ACTION_DOWN) {
				Log.d("OverlayService", "Touch event")
			}
			false
		}*/

		// Trick The ComposeView into thinking we are tracking lifecycle
		/*val viewModelStore = ViewModelStore()
		val lifecycleOwner = MyLifecycleOwner()
		lifecycleOwner.performRestore(null)
		lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
		ViewTreeLifecycleOwner.set(composeView, lifecycleOwner)
		ViewTreeViewModelStoreOwner.set(composeView) { viewModelStore }
		composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)*/

		val viewModelStore = ViewModelStore()
		val viewModelStoreOwner = object : ViewModelStoreOwner {
			override val viewModelStore: ViewModelStore
				get() = viewModelStore
		}

		val lifecycleOwner = MyLifecycleOwner()
		lifecycleOwner.performRestore(null)
		lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        overlayLifecycle = lifecycleOwner
		composeView.setViewTreeLifecycleOwner(lifecycleOwner)
		composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
		composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)

		// Make recomposition happen on the UI thread
		val coroutineContext = AndroidUiDispatcher.CurrentThread
		val runRecomposeScope = CoroutineScope(SupervisorJob() + coroutineContext)
        recompositionScope = runRecomposeScope
		val recomposer = Recomposer(coroutineContext)
        activeRecomposer = recomposer
		composeView.compositionContext = recomposer
		runRecomposeScope.launch {
			recomposer.runRecomposeAndApplyChanges()
		}

		// Add the view to the window
		windowManager.addView(composeView, params)
        overlayView = composeView
	}

	override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    private fun cleanup() {
        if (cleanedUp) return
        cleanedUp = true
        if (receiverRegistered) {
            runCatching { unregisterReceiver(mBroadcastReceiver) }
            receiverRegistered = false
        }
        if (::pluginManager.isInitialized) pluginManager.onDestroy()
        overlayView?.let { view -> runCatching { overlayWindowManager?.removeViewImmediate(view) } }
        overlayView = null
        overlayWindowManager = null
        activeRecomposer?.close()
        activeRecomposer = null
        recompositionScope?.cancel()
        recompositionScope = null
        overlayLifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        overlayLifecycle = null
        instance = null
    }

	fun expand() { islandState = IslandViewState.Expanded(configuration = resources.configuration) }
	fun shrink() { islandState = IslandViewState.Opened }

	override fun requestDisplay(plugin: BasePlugin) {
		pluginManager.requestDisplay(plugin)
	}

	override fun requestDismiss(plugin: BasePlugin) {
		pluginManager.requestDismiss(plugin)
	}

	override fun requestExpand() {
		expand()
	}

	override fun requestShrink() {
		shrink()
	}

	override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
		return super.onUnbind(intent)
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		Island.isInLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (islandState is IslandViewState.Expanded) {
            islandState = IslandViewState.Expanded(newConfig)
        }
	}

	override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
	override fun onInterrupt() {}
}
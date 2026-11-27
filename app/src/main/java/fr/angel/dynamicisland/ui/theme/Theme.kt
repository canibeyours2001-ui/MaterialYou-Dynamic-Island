package fr.angel.dynamicisland.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import fr.angel.dynamicisland.model.SETTINGS_KEY
import fr.angel.dynamicisland.model.STYLE
import fr.angel.dynamicisland.model.THEME
import fr.angel.dynamicisland.ui.theme.themes.BlackTheme
import fr.angel.dynamicisland.ui.theme.themes.QuinacridoneMagentaThemeDarkColors
import fr.angel.dynamicisland.ui.theme.themes.QuinacridoneMagentaThemeLightColors

class Theme {

	enum class ThemeStyle(
		val lightScheme: ColorScheme? = null,
		val darkScheme: ColorScheme? = null,
		val styleName: String,
		val previewColorLight: Color?,
		val previewColorDark: Color?
	) {
		MaterialYou(
			styleName = "Material You",
			previewColorLight = null,
			previewColorDark = null
		),
		Black(
			darkScheme = BlackTheme,
			styleName = "Black & White",
			previewColorLight = Color.Black,
			previewColorDark = Color.White
		),
		QuinacridoneMagenta(
			lightScheme = QuinacridoneMagentaThemeLightColors,
			darkScheme = QuinacridoneMagentaThemeDarkColors,
			styleName = "Quinacridone Magenta",
			previewColorLight = QuinacridoneMagentaThemeLightColors.primary,
			previewColorDark = QuinacridoneMagentaThemeDarkColors.primary
		),
	}

	companion object {
		val instance = Theme()
	}

	var isDarkTheme by mutableStateOf(false)
	var themeStyle by mutableStateOf(ThemeStyle.MaterialYou)

	@Composable
	fun Init(
		isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
	) {
		val context = LocalContext.current
		val settingsPreferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

		isDarkTheme = when (settingsPreferences.getString(THEME, "System")) {
			"System" -> { isSystemInDarkTheme }
			"Dark" -> { true }
			"Light" -> { false }
			else -> { isSystemInDarkTheme }
		}

		themeStyle = when (settingsPreferences.getString(STYLE, "MaterialYou")) {
			ThemeStyle.MaterialYou.name -> { ThemeStyle.MaterialYou }
			ThemeStyle.Black.name -> { ThemeStyle.Black }
			ThemeStyle.QuinacridoneMagenta.name -> { ThemeStyle.QuinacridoneMagenta }
			else -> { ThemeStyle.MaterialYou }
		}
	}
}

@Composable
fun DynamicIslandTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	style: Theme.ThemeStyle = Theme.instance.themeStyle,
	// Dynamic color is available on Android 12+
	content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // An AccessibilityService has no Activity window. Only update system bars
    // when this theme is actually hosted inside a normal Activity.
    val activity = remember(context) { context.findActivity() }
    SideEffect {
        activity?.let {
            val darkIcons = when (style) {
                Theme.ThemeStyle.MaterialYou -> !darkTheme
                else -> if (darkTheme) style.darkScheme == null else style.lightScheme != null
            }
            val controller = WindowCompat.getInsetsController(it.window, it.window.decorView)
            controller.isAppearanceLightStatusBars = darkIcons
            controller.isAppearanceLightNavigationBars = darkIcons
        }
    }

	MaterialTheme(
		colorScheme = when(style) {
			Theme.ThemeStyle.MaterialYou -> {
				if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
			}
			else -> {
				if (darkTheme) {
					style.darkScheme ?: style.lightScheme ?: dynamicDarkColorScheme(context)
				} else {
					style.lightScheme ?: style.darkScheme ?: dynamicLightColorScheme(context)
				}
			}
		},
		typography = Typography,
		content = content
	)
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

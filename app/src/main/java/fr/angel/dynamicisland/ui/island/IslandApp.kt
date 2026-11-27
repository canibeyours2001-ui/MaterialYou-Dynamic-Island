package fr.angel.dynamicisland.ui.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.angel.dynamicisland.island.Island
import fr.angel.dynamicisland.island.IslandGravity
import fr.angel.dynamicisland.island.IslandSettings
import fr.angel.dynamicisland.island.IslandViewState
import fr.angel.dynamicisland.model.service.IslandOverlayService
import fr.angel.dynamicisland.ui.theme.DynamicIslandTheme
import fr.angel.dynamicisland.ui.theme.Theme


@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun IslandApp(
    islandOverlayService: IslandOverlayService
) {
    val context = LocalContext.current
    Theme.instance.Init()
    LaunchedEffect(Unit) {
        IslandSettings.instance.loadSettings(context = context)
    }

    val islandView = islandOverlayService.islandState
    val bindedPlugin = islandOverlayService.pluginManager.currentPlugin

    // One interruptible spring for both the container and its content: no one-second
    // delayed shrinking, snapping or layout desynchronization on rapid activity changes.
    val reducedMotion = IslandSettings.instance.reducedMotion
    val speed = IslandSettings.instance.animationSpeed
    val height by animateDpAsState(
        targetValue = islandView.height,
        animationSpec = if (reducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow * speed
        ),
        label = "island-height"
    )
    val width by animateDpAsState(
        targetValue = islandView.width,
        animationSpec = if (reducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow * speed
        ),
        label = "island-width"
    )
    val cornerPercentage by animateFloatAsState(
        targetValue = islandView.cornerPercentage,
        animationSpec = if (reducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow * speed
        ),
        label = "island-shape"
    )

    val swipeModifier = if (islandView is IslandViewState.Opened) {
        Modifier.pointerInput(bindedPlugin?.id) {
            var horizontalTravel = 0f
            detectHorizontalDragGestures(
                onDragStart = { horizontalTravel = 0f },
                onDragEnd = {
                    if (abs(horizontalTravel) > 48f) {
                        islandOverlayService.pluginManager.cycleActivity(
                            if (horizontalTravel < 0f) 1 else -1
                        )
                    }
                    horizontalTravel = 0f
                },
                onHorizontalDrag = { _, delta ->
                    // The gesture detector consumes drag changes after touch slop.
                    horizontalTravel += delta
                }
            )
        }
    } else Modifier

    val clickModifier =
        if (islandView is IslandViewState.Opened || islandView is IslandViewState.Expanded) {
            Modifier
                .combinedClickable(
                    onClick = { bindedPlugin?.onClick() },
                    onLongClick = {
                        if (islandView is IslandViewState.Expanded) {
                            islandOverlayService.shrink()
                        } else if (bindedPlugin?.canExpand() == true) {
                            islandOverlayService.expand()
                        }
                    }
                )
        } else {
            Modifier
        }

    AnimatedVisibility(
        visible = (bindedPlugin != null || IslandSettings.instance.showIdleIsland)
                && (Island.isScreenOn || IslandSettings.instance.showOnLockScreen)
                && (!Island.isInLandscape || IslandSettings.instance.showInLandscape),
        modifier = Modifier.fillMaxWidth()
    ) {
        DynamicIslandTheme(
            darkTheme = if (islandOverlayService.invertedTheme) !Theme.instance.isDarkTheme else Theme.instance.isDarkTheme,
            style = Theme.instance.themeStyle
        ) {
            SharedTransitionLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = when (IslandSettings.instance.gravity) {
                        IslandGravity.Center -> Alignment.TopCenter
                        IslandGravity.Left -> Alignment.TopStart
                        IslandGravity.Right -> Alignment.TopEnd
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = islandView.yPosition)
                            .width(width)
                            .height(height)
                            .offset(x = islandView.xPosition),
                        contentAlignment = when (IslandSettings.instance.gravity) {
                            IslandGravity.Center -> Alignment.TopCenter
                            IslandGravity.Left -> Alignment.TopStart
                            IslandGravity.Right -> Alignment.TopEnd
                        }
                    ) {

                        val borderModifier = if (IslandSettings.instance.showBorders) {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(cornerPercentage)
                            )
                        } else {
                            Modifier
                        }

                        Card(
                            shape = RoundedCornerShape(cornerPercentage),
                            modifier = Modifier
                                .then(borderModifier)
                                .width(width)
                                .height(height)
                                .defaultMinSize(minHeight = height)
                                .then(clickModifier)
                                .then(swipeModifier),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            )
                        ) {
                            val boxModifier = Modifier.fillMaxHeight()

                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                AnimatedContent(
                                    targetState = islandView,
                                    transitionSpec = {
                                        if (reducedMotion) {
                                            EnterTransition.None togetherWith ExitTransition.None
                                        } else {
                                            fadeIn(animationSpec = tween(160)) togetherWith
                                                fadeOut(animationSpec = tween(130))
                                        }
                                    },
                                    label = "IslandTransition"
                                ) { islandView ->
                                    // Expanded State
                                    if (islandView is IslandViewState.Expanded) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .sharedBounds(
                                                    sharedContentState = rememberSharedContentState(
                                                        key = "ExpandedContent",
                                                    ),
                                                    animatedVisibilityScope = this@AnimatedContent,
                                                )
                                                .clip(RoundedCornerShape(cornerPercentage))
                                        ) {
                                            bindedPlugin?.Composable(
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedContentScope = this@AnimatedContent
                                            )
                                        }
                                    } else {
                                        // Opened State
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp)
                                                .sharedBounds(
                                                    sharedContentState = rememberSharedContentState(
                                                        key = "ExpandedContent",
                                                    ),
                                                    animatedVisibilityScope = this@AnimatedContent
                                                )
                                                .clip(RoundedCornerShape(cornerPercentage)),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Box(
                                                modifier = boxModifier,
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                bindedPlugin?.LeftOpenedComposable(
                                                    sharedTransitionScope = this@SharedTransitionLayout,
                                                    animatedContentScope = this@AnimatedContent
                                                )
                                            }

                                            Box(
                                                modifier = boxModifier,
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                bindedPlugin?.RightOpenedComposable(
                                                    sharedTransitionScope = this@SharedTransitionLayout,
                                                    animatedContentScope = this@AnimatedContent
                                                )
                                            }
                                        }
                                    }
                                }
                            }


                        }
                    }
                }
            }
        }
    }
}
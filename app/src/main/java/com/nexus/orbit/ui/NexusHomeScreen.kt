package com.nexus.orbit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nexus.orbit.animation.AvatarAnimationController
import com.nexus.orbit.launch.AppLaunchController
import com.nexus.orbit.launch.LaunchOriginMapper
import com.nexus.orbit.launcher.InstalledApp
import com.nexus.orbit.launcher.InstalledAppRepository
import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.launcher.LauncherState
import com.nexus.orbit.scene.AppRingController
import com.nexus.orbit.scene.AvatarRigController
import com.nexus.orbit.scene.NexusLauncherScene
import com.nexus.orbit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun NexusHomeScreen(
    state: LauncherState,
    settings: LauncherSettings,
    repository: InstalledAppRepository,
    launchController: AppLaunchController
) {
    val apps by repository.installedApps.collectAsState()
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val density = LocalDensity.current.density
    val lifecycleOwner = LocalLifecycleOwner.current

    var rigController by remember { mutableStateOf<AvatarRigController?>(null) }
    val animController = remember(rigController, state) {
        AvatarAnimationController(rigController, state, scope)
    }

    // Lifecycle Observer: Resets hands to your calibrated pose whenever you return home from an app
    DisposableEffect(lifecycleOwner, animController) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state.isLaunching = false
                animController.resetToSavedNeutralPose()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val ringController = remember { AppRingController() }
    var viewportWidth by remember { mutableFloatStateOf(1080f) }
    var viewportHeight by remember { mutableFloatStateOf(2400f) }

    val projectedApps = remember(apps.size, state.ringAngle, viewportWidth, viewportHeight) {
        ringController.computeOrbit(apps.size, state.ringAngle, viewportWidth, viewportHeight)
    }

    val frontItem = projectedApps.firstOrNull { it.isFront } ?: projectedApps.firstOrNull()
    val selectedApp: InstalledApp? = frontItem?.let { apps.getOrNull(it.index) }

    // Idle rotation with selection pause timer
    LaunchedEffect(state.isDragging, state.isLaunching, settings.idleRotation, settings.pauseDurationSeconds, apps.size) {
        while (isActive) {
            val now = System.currentTimeMillis()
            val pauseMs = settings.pauseDurationSeconds * 1000L
            val isPaused = (now - state.lastInteractionTime) < pauseMs

            if (!state.isDragging && !state.isLaunching && settings.idleRotation && !isPaused && apps.isNotEmpty()) {
                val speed = 0.0022f * settings.orbitSpeed
                state.ringAngle = (state.ringAngle + speed) % (2f * Math.PI.toFloat())
            }
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitDeepBlack)
            .onGloballyPositioned { coordinates ->
                viewportWidth = coordinates.size.width.toFloat()
                viewportHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(state.isLaunching, apps.size) {
                if (state.isLaunching || apps.isEmpty()) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        state.isDragging = true
                        state.lastInteractionTime = System.currentTimeMillis()
                    },
                    onDragEnd = {
                        state.isDragging = false
                        state.lastInteractionTime = System.currentTimeMillis()
                        state.ringAngle = ringController.calculateSnapAngle(apps.size, state.ringAngle)
                    },
                    onDragCancel = {
                        state.isDragging = false
                        state.lastInteractionTime = System.currentTimeMillis()
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        state.lastInteractionTime = System.currentTimeMillis()
                        val sensitivity = 0.0035f * settings.orbitSpeed
                        state.ringAngle -= dragAmount * sensitivity
                    }
                )
            }
    ) {
        // 1. Screen-filling 3D Room & Real Avatar
        NexusLauncherScene(
            state = state,
            settings = settings,
            onRigControllerReady = { rigController = it },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Visible Front Apps Carousel
        for (item in projectedApps) {
            val app = apps.getOrNull(item.index) ?: continue
            val isSelected = item.isFront

            Box(
                modifier = Modifier
                    .offset(
                        x = (item.screenX / density).dp - 36.dp,
                        y = (item.screenY / density).dp - 36.dp
                    )
                    .size(72.dp)
                    .scale(item.scale)
                    .alpha(item.alpha)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.5.dp, OrbitCyanAccent, CircleShape)
                        } else Modifier
                    )
                    .clickable(enabled = !state.isLaunching) {
                        if (isSelected) {
                            state.isLaunching = true
                            val screenBounds = LaunchOriginMapper.computeScreenBounds(
                                item.screenX,
                                item.screenY,
                                72f,
                                density
                            )
                            animController.executeAppLaunchSequence(
                                speedMultiplier = settings.animSpeed,
                                onGrabTriggered = {
                                    launchController.launchApplication(app, screenBounds, view)
                                },
                                onSequenceCompleted = {
                                    state.isLaunching = false
                                }
                            )
                        } else {
                            state.lastInteractionTime = System.currentTimeMillis()
                            val step = (2.0 * Math.PI / apps.size).toFloat()
                            state.ringAngle = -item.index * step
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = app.icon.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(if (isSelected) 60.dp else 52.dp)
                )
            }
        }

        // 3. Selected App Label HUD
        if (selectedApp != null && !state.isLaunching) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedApp.label,
                    color = OrbitTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to Launch",
                    color = OrbitCyanAccent,
                    fontSize = 12.sp
                )
            }
        }

        // 4. Discreet Settings Entry
        IconButton(
            onClick = { state.showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Launcher Settings",
                tint = OrbitTextSecondary
            )
        }

        if (state.showSettings) {
            LauncherSettingsScreen(
                state = state,
                settings = settings,
                appRepository = repository,
                rigController = rigController,
                onOpenDiagnostics = {
                    state.showSettings = false
                    state.showDiagnostics = true
                },
                onDismiss = { state.showSettings = false }
            )
        }

        if (state.showDiagnostics) {
            AvatarRigDiagnosticsScreen(
                controller = rigController,
                settings = settings,
                onDismiss = { state.showDiagnostics = false }
            )
        }
    }
}
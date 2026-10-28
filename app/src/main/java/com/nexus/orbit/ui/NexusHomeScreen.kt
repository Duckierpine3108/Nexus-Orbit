package com.nexus.orbit.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var rigController by remember { mutableStateOf<AvatarRigController?>(null) }
    val animController = remember(rigController, state) {
        AvatarAnimationController(rigController, state, scope)
    }

    // Dynamic Look-At Tracking:
    // Dragging: tracks finger/orbit and stops at natural limit (+-22 deg)
    // Idle: alternates glances right for ~6s, center at you, and left for ~6s
    LaunchedEffect(state.isDragging, state.ringAngle, state.headPitch, state.headYaw, rigController) {
        while (isActive) {
            if (state.isDragging) {
                val dragYaw = (kotlin.math.sin(state.ringAngle) * 22f).coerceIn(-22f, 22f)
                rigController?.updateHeadPose(state.headPitch, state.headYaw + dragYaw)
            } else if (!state.isLaunching) {
                val timeSec = System.currentTimeMillis() / 1000.0
                val idleYaw = (kotlin.math.sin(timeSec * 0.65) * 16f).toFloat()
                rigController?.updateHeadPose(state.headPitch, state.headYaw + idleYaw)
            }
            delay(16)
        }
    }

    LaunchedEffect(state.isLaunching) {
        if (!state.isLaunching) {
            animController.resetToSavedNeutralPose()
        }
    }

    val ringController = remember { AppRingController() }
    var viewportWidth by remember { mutableFloatStateOf(1080f) }
    var viewportHeight by remember { mutableFloatStateOf(2400f) }

    val projectedApps = remember(apps.size, state.ringAngle, viewportWidth, viewportHeight, state.ringHeightRatio) {
        ringController.computeOrbit(apps.size, state.ringAngle, viewportWidth, viewportHeight, state.ringHeightRatio)
    }

    val frontItem = projectedApps.firstOrNull { it.isFront } ?: projectedApps.firstOrNull()
    val selectedApp: InstalledApp? = frontItem?.let { apps.getOrNull(it.index) }

    // Idle rotation with pause timer
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
        NexusLauncherScene(
            state = state,
            settings = settings,
            onRigControllerReady = { rigController = it },
            modifier = Modifier.fillMaxSize()
        )

        // Subtle Ground Shadow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
                .size(width = 320.dp, height = 45.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2A00E5FF), Color.Transparent)
                    )
                )
        )

        // 3D Orbital Carousel with Physical Push Feedback
        for (item in projectedApps) {
            val app = apps.getOrNull(item.index) ?: continue
            val isSelected = item.isFront

            // When selected app is clicked, scales down to 0.88x as left hand pushes it!
            val tapScale by animateFloatAsState(
                targetValue = if (isSelected && state.isLaunching) 0.88f else item.scale,
                animationSpec = tween(durationMillis = 140),
                label = "tapScale"
            )

            Box(
                modifier = Modifier
                    .offset(
                        x = (item.screenX / density).dp - 36.dp,
                        y = (item.screenY / density).dp - 36.dp
                    )
                    .size(72.dp)
                    .graphicsLayer {
                        this.scaleX = tapScale
                        this.scaleY = tapScale
                        this.alpha = item.alpha
                    }
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0x3300E5FF), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                                .border(2.5.dp, OrbitCyanAccent, CircleShape)
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

        // HUD App Label
        if (selectedApp != null && !state.isLaunching) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp),
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

        // Settings Entry
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
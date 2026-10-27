package com.nexus.orbit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
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
    val animController = remember(rigController) {
        AvatarAnimationController(rigController, scope)
    }

    val ringController = remember { AppRingController() }
    var viewportWidth by remember { mutableFloatStateOf(1080f) }
    var viewportHeight by remember { mutableFloatStateOf(2400f) }

    val projectedApps = remember(apps.size, state.ringAngle, viewportWidth, viewportHeight) {
        ringController.computeOrbit(apps.size, state.ringAngle, viewportWidth, viewportHeight)
    }

    val frontItem = projectedApps.lastOrNull { it.isFront } ?: projectedApps.lastOrNull()
    val selectedApp: InstalledApp? = frontItem?.let { apps.getOrNull(it.index) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitDeepBlack)
            .onGloballyPositioned { coordinates ->
                viewportWidth = coordinates.size.width.toFloat()
                viewportHeight = coordinates.size.height.toFloat()
            }
    ) {
        // 1. Native 3D Room & Real Skinned Avatar
        NexusLauncherScene(
            state = state,
            settings = settings,
            onRigControllerReady = { rigController = it },
            modifier = Modifier.fillMaxSize()
        )

        // 2. 3D Projected Orbital Application Ring
        for (item in projectedApps) {
            val app = apps.getOrNull(item.index) ?: continue
            val isSelected = (item.index == frontItem?.index)

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
                            Modifier.border(2.dp, OrbitCyanAccent, CircleShape)
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
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = app.icon.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(if (isSelected) 60.dp else 52.dp)
                )
            }
        }

        // 3. Front Selected App Name HUD
        if (selectedApp != null && !state.isLaunching) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
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

        // 4. Discreet Settings Entry Button
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

        // Settings Dialog Overlay
        if (state.showSettings) {
            LauncherSettingsScreen(
                settings = settings,
                appRepository = repository,
                onOpenDiagnostics = {
                    state.showSettings = false
                    state.showDiagnostics = true
                },
                onDismiss = { state.showSettings = false }
            )
        }

        // Avatar Rig Diagnostics Screen
        if (state.showDiagnostics) {
            AvatarRigDiagnosticsScreen(
                controller = rigController,
                settings = settings,
                onDismiss = { state.showDiagnostics = false }
            )
        }
    }
}
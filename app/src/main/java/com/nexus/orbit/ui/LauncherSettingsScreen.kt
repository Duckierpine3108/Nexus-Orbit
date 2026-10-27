package com.nexus.orbit.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.orbit.launcher.InstalledAppRepository
import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.launcher.LauncherState
import com.nexus.orbit.scene.AvatarRigController
import com.nexus.orbit.ui.theme.*

@Composable
fun LauncherSettingsScreen(
    state: LauncherState,
    settings: LauncherSettings,
    appRepository: InstalledAppRepository,
    rigController: AvatarRigController?,
    onOpenDiagnostics: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var pauseDuration by remember { mutableIntStateOf(settings.pauseDurationSeconds) }

    fun updateArmPose() {
        rigController?.updateCustomPose(
            state.rightArmX, state.rightArmY, state.rightArmZ,
            state.leftArmX, state.leftArmY, state.leftArmZ
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OrbitDeepBlack.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Settings & Avatar Posing", color = OrbitCyanAccent, fontSize = 20.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = OrbitTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Avatar Framing & Height (Interactive Live Sliders)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrbitSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Avatar Framing (Live Adjustment)", color = OrbitCyanAccent, fontSize = 14.sp)

                    Text("Vertical Position (Y): ${String.format("%.2f", state.avatarOffsetY)}m", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.avatarOffsetY,
                        onValueChange = {
                            state.avatarOffsetY = it
                            settings.avatarOffsetY = it
                        },
                        valueRange = -1.5f..1.0f
                    )

                    Text("Avatar Size / Zoom: ${String.format("%.2f", state.avatarScale)}x", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.avatarScale,
                        onValueChange = {
                            state.avatarScale = it
                            settings.avatarScale = it
                        },
                        valueRange = 0.8f..2.5f
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Right Arm Sliders
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrbitSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Right Arm Rotation (Degrees)", color = OrbitCyanAccent, fontSize = 14.sp)

                    Text("Pitch (X): ${state.rightArmX.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.rightArmX,
                        onValueChange = { state.rightArmX = it; settings.rightArmX = it; updateArmPose() },
                        valueRange = -180f..180f
                    )

                    Text("Yaw (Y): ${state.rightArmY.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.rightArmY,
                        onValueChange = { state.rightArmY = it; settings.rightArmY = it; updateArmPose() },
                        valueRange = -180f..180f
                    )

                    Text("Roll / Lower (Z): ${state.rightArmZ.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.rightArmZ,
                        onValueChange = { state.rightArmZ = it; settings.rightArmZ = it; updateArmPose() },
                        valueRange = -180f..180f
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Left Arm Sliders
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrbitSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Left Arm Rotation (Degrees)", color = OrbitCyanAccent, fontSize = 14.sp)

                    Text("Pitch (X): ${state.leftArmX.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.leftArmX,
                        onValueChange = { state.leftArmX = it; settings.leftArmX = it; updateArmPose() },
                        valueRange = -180f..180f
                    )

                    Text("Yaw (Y): ${state.leftArmY.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.leftArmY,
                        onValueChange = { state.leftArmY = it; settings.leftArmY = it; updateArmPose() },
                        valueRange = -180f..180f
                    )

                    Text("Roll / Lower (Z): ${state.leftArmZ.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = state.leftArmZ,
                        onValueChange = { state.leftArmZ = it; settings.leftArmZ = it; updateArmPose() },
                        valueRange = -180f..180f
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Launcher Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrbitSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Interaction Timer", color = OrbitCyanAccent, fontSize = 14.sp)
                    Text("Selection Pause: ${pauseDuration}s", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = pauseDuration.toFloat(),
                        onValueChange = {
                            pauseDuration = it.toInt()
                            settings.pauseDurationSeconds = it.toInt()
                        },
                        valueRange = 2f..15f,
                        steps = 12
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitCyanAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Set as Default Launcher", color = OrbitDeepBlack)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onOpenDiagnostics,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitSurfaceElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Open Full Rig Diagnostics", color = OrbitCyanAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
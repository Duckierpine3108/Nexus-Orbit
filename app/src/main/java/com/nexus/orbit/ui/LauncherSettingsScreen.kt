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
import com.nexus.orbit.scene.AvatarRigController
import com.nexus.orbit.ui.theme.*

@Composable
fun LauncherSettingsScreen(
    settings: LauncherSettings,
    appRepository: InstalledAppRepository,
    rigController: AvatarRigController?,
    onOpenDiagnostics: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var avatarOffsetY by remember { mutableFloatStateOf(settings.avatarOffsetY) }
    var avatarScale by remember { mutableFloatStateOf(settings.avatarScale) }

    var rX by remember { mutableFloatStateOf(settings.rightArmX) }
    var rY by remember { mutableFloatStateOf(settings.rightArmY) }
    var rZ by remember { mutableFloatStateOf(settings.rightArmZ) }

    var lX by remember { mutableFloatStateOf(settings.leftArmX) }
    var lY by remember { mutableFloatStateOf(settings.leftArmY) }
    var lZ by remember { mutableFloatStateOf(settings.leftArmZ) }

    var pauseDuration by remember { mutableIntStateOf(settings.pauseDurationSeconds) }

    fun updateArmPose() {
        rigController?.updateCustomPose(rX, rY, rZ, lX, lY, lZ)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OrbitDeepBlack.copy(alpha = 0.84f)
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

            // 1. Avatar Framing & Height (Expanded scale up to 5.50x)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrbitSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Avatar Framing & Screen Coverage", color = OrbitCyanAccent, fontSize = 14.sp)

                    Text("Vertical Position (Y): ${String.format("%.2f", avatarOffsetY)}m", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = avatarOffsetY,
                        onValueChange = {
                            avatarOffsetY = it
                            settings.avatarOffsetY = it
                        },
                        valueRange = -1.0f..2.5f
                    )

                    Text("Avatar Size: ${String.format("%.2f", avatarScale)}x", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = avatarScale,
                        onValueChange = {
                            avatarScale = it
                            settings.avatarScale = it
                        },
                        valueRange = 1.5f..5.5f
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

                    Text("Pitch (X): ${rX.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(value = rX, onValueChange = { rX = it; settings.rightArmX = it; updateArmPose() }, valueRange = -180f..180f)

                    Text("Yaw (Y): ${rY.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(value = rY, onValueChange = { rY = it; settings.rightArmY = it; updateArmPose() }, valueRange = -180f..180f)

                    Text("Roll / Lower (Z): ${rZ.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(value = rZ, onValueChange = { rZ = it; settings.rightArmZ = it; updateArmPose() }, valueRange = -180f..180f)
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

                    Text("Pitch (X): ${lX.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(value = lX, onValueChange = { lX = it; settings.leftArmX = it; updateArmPose() }, valueRange = -180f..180f)

                    Text("Yaw (Y): ${lY.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(value = lY, onValueChange = { lY = it; settings.leftArmY = it; updateArmPose() }, valueRange = -180f..180f)

                    Text("Roll / Lower (Z): ${lZ.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(value = lZ, onValueChange = { lZ = it; settings.leftArmZ = it; updateArmPose() }, valueRange = -180f..180f)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Launcher System Settings
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
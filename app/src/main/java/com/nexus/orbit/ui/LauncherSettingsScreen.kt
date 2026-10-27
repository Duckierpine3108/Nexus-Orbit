package com.nexus.orbit.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.nexus.orbit.ui.theme.*

@Composable
fun LauncherSettingsScreen(
    settings: LauncherSettings,
    appRepository: InstalledAppRepository,
    onOpenDiagnostics: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var orbitSpeed by remember { mutableFloatStateOf(settings.orbitSpeed) }
    var idleRotation by remember { mutableStateOf(settings.idleRotation) }
    var pauseDuration by remember { mutableIntStateOf(settings.pauseDurationSeconds) }
    var animSpeed by remember { mutableFloatStateOf(settings.animSpeed) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OrbitDeepBlack.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Launcher Settings", color = OrbitCyanAccent, fontSize = 22.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = OrbitTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OrbitCyanAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Set as Default Launcher", color = OrbitDeepBlack)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Idle Orbital Rotation", color = OrbitTextPrimary, fontSize = 16.sp)
                Switch(
                    checked = idleRotation,
                    onCheckedChange = {
                        idleRotation = it
                        settings.idleRotation = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pause duration setting (default 5s)
            Text("Selection Pause Duration: ${pauseDuration}s", color = OrbitTextSecondary)
            Slider(
                value = pauseDuration.toFloat(),
                onValueChange = {
                    pauseDuration = it.toInt()
                    settings.pauseDurationSeconds = it.toInt()
                },
                valueRange = 2f..15f,
                steps = 12
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Orbit Speed: ${String.format("%.1fx", orbitSpeed)}", color = OrbitTextSecondary)
            Slider(
                value = orbitSpeed,
                onValueChange = {
                    orbitSpeed = it
                    settings.orbitSpeed = it
                },
                valueRange = 0.2f..3.0f
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Avatar Animation Speed: ${String.format("%.1fx", animSpeed)}", color = OrbitTextSecondary)
            Slider(
                value = animSpeed,
                onValueChange = {
                    animSpeed = it
                    settings.animSpeed = it
                },
                valueRange = 0.5f..2.5f
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    appRepository.refreshApps()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OrbitSurfaceElevated),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Refresh Installed Apps", color = OrbitTextPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenDiagnostics,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OrbitSurfaceElevated),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open Avatar Rig Diagnostics", color = OrbitCyanAccent)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "NEXUS ORBIT v1.0.0 • Redmi Note 9S curtana build",
                color = OrbitTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
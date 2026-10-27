package com.nexus.orbit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.orbit.animation.AvatarPose
import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.scene.AvatarRigController
import com.nexus.orbit.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarRigDiagnosticsScreen(
    controller: AvatarRigController?,
    settings: LauncherSettings,
    onDismiss: () -> Unit
) {
    var selectedBoneName by remember { mutableStateOf("") }
    var rotX by remember { mutableFloatStateOf(0f) }
    var rotY by remember { mutableFloatStateOf(0f) }
    var rotZ by remember { mutableFloatStateOf(0f) }

    val boneList = remember(controller) {
        controller?.discoveredJoints?.keys?.toList()?.sorted() ?: emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OrbitDeepBlack.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avatar Rig Diagnostics",
                    color = OrbitCyanAccent,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = OrbitTextPrimary)
                }
            }

            Text(
                text = "Inspect and test detected skeletal joints without external clips.",
                color = OrbitTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Pose Test Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { controller?.applyPose(AvatarPose.NEUTRAL_IDLE) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitSurfaceElevated),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Neutral Pose", fontSize = 12.sp)
                }
                Button(
                    onClick = { controller?.applyPose(AvatarPose.REACH) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitSurfaceElevated),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reach Pose", fontSize = 12.sp)
                }
                Button(
                    onClick = { controller?.resetToBindPose() },
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitSurfaceElevated),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Bind", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Joint Manual Rotation Tester
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrbitSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Joint Tester: $selectedBoneName", color = OrbitTextPrimary, fontSize = 14.sp)

                    Text("Rotation X: ${rotX.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = rotX,
                        onValueChange = {
                            rotX = it
                            controller?.discoveredJoints?.get(selectedBoneName)?.let { id ->
                                controller.applyDirectJointRotation(id, rotX, rotY, rotZ)
                            }
                        },
                        valueRange = -180f..180f
                    )

                    Text("Rotation Y: ${rotY.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = rotY,
                        onValueChange = {
                            rotY = it
                            controller?.discoveredJoints?.get(selectedBoneName)?.let { id ->
                                controller.applyDirectJointRotation(id, rotX, rotY, rotZ)
                            }
                        },
                        valueRange = -180f..180f
                    )

                    Text("Rotation Z: ${rotZ.toInt()}°", color = OrbitTextSecondary, fontSize = 12.sp)
                    Slider(
                        value = rotZ,
                        onValueChange = {
                            rotZ = it
                            controller?.discoveredJoints?.get(selectedBoneName)?.let { id ->
                                controller.applyDirectJointRotation(id, rotX, rotY, rotZ)
                            }
                        },
                        valueRange = -180f..180f
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Discovered Joint Hierarchy (${boneList.size} bones):", color = OrbitTextPrimary, fontSize = 14.sp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
            ) {
                items(boneList) { bone ->
                    val isMapped = bone == selectedBoneName
                    Surface(
                        onClick = {
                            selectedBoneName = bone
                            rotX = 0f; rotY = 0f; rotZ = 0f
                        },
                        color = if (isMapped) OrbitSurfaceElevated else OrbitSurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = bone,
                            color = if (isMapped) OrbitCyanAccent else OrbitTextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
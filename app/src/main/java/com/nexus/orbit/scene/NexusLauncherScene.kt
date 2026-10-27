package com.nexus.orbit.scene

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.nexus.orbit.animation.AvatarPose
import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.launcher.LauncherState
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader

@Composable
fun NexusLauncherScene(
    state: LauncherState,
    settings: LauncherSettings,
    onRigControllerReady: (AvatarRigController) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // Key Light: High intensity directional light to illuminate avatar against black background
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 160_000.0f
        position = Position(x = 0.0f, y = 3.2f, z = 3.5f)
        rotation = Rotation(x = -30.0f, y = 0.0f, z = 0.0f)
    }

    // Camera framed on the avatar's upper body and chest
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0.0f, y = 1.15f, z = 3.1f)
        rotation = Rotation(x = -2.0f, y = 0.0f, z = 0.0f)
    }

    var modelNode by remember { mutableStateOf<ModelNode?>(null) }

    LaunchedEffect(Unit) {
        val modelPath = "models/nexus_avatar.glb"
        try {
            val assetExists = context.assets.list("models")?.contains("nexus_avatar.glb") == true
            if (!assetExists) {
                Log.e("NexusLauncherScene", "nexus_avatar.glb not found in assets/models/")
                return@LaunchedEffect
            }

            val modelInstance = modelLoader.createModelInstance(modelPath)
            if (modelInstance != null) {
                val node = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 1.82f
                ).apply {
                    position = Position(x = 0f, y = 0f, z = 0f)
                }
                modelNode = node

                val controller = AvatarRigController(
                    engine = engine,
                    asset = modelInstance.asset,
                    animator = modelInstance.animator
                )
                onRigControllerReady(controller)
                controller.applyPose(AvatarPose.NEUTRAL_IDLE)
            }
        } catch (e: Exception) {
            Log.e("NexusLauncherScene", "Error loading avatar GLB", e)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
            childNodes = listOfNotNull(modelNode)
        )
    }
}
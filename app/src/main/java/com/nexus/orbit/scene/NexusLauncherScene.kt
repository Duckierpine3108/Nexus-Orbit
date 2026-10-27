package com.nexus.orbit.scene

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.launcher.LauncherState
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
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

    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 170_000.0f
        position = Position(x = 0.0f, y = 3.5f, z = 3.5f)
        rotation = Rotation(x = -30.0f, y = 0.0f, z = 0.0f)
    }

    // Closer camera distance (z = 2.35m) for screen-filling portrait view
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0.0f, y = 0.95f, z = 2.35f)
        rotation = Rotation(x = -1.0f, y = 0.0f, z = 0.0f)
    }

    var modelNode by remember { mutableStateOf<ModelNode?>(null) }

    LaunchedEffect(Unit) {
        val modelPath = "models/nexus_avatar.glb"
        try {
            val assetExists = context.assets.list("models")?.contains("nexus_avatar.glb") == true
            if (!assetExists) {
                Log.e("NexusLauncherScene", "nexus_avatar.glb not found")
                return@LaunchedEffect
            }

            val modelInstance = modelLoader.createModelInstance(modelPath)
            if (modelInstance != null) {
                val node = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = settings.avatarScale
                ).apply {
                    position = Position(x = 0f, y = settings.avatarOffsetY, z = 0f)
                }
                modelNode = node

                val controller = AvatarRigController(
                    engine = engine,
                    asset = modelInstance.asset,
                    animator = modelInstance.animator
                )
                onRigControllerReady(controller)

                // Apply your exact calibrated numbers on launch
                controller.updateCustomPose(
                    rX = settings.rightArmX,
                    rY = settings.rightArmY,
                    rZ = settings.rightArmZ,
                    lX = settings.leftArmX,
                    lY = settings.leftArmY,
                    lZ = settings.leftArmZ
                )
            }
        } catch (e: Exception) {
            Log.e("NexusLauncherScene", "Error loading avatar GLB", e)
        }
    }

    // Dynamic scale and height updates from Settings
    LaunchedEffect(settings.avatarOffsetY, settings.avatarScale) {
        modelNode?.let {
            it.position = Position(0f, settings.avatarOffsetY, 0f)
            it.scale = Scale(settings.avatarScale / 1.8f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            cameraManipulator = null,
            mainLightNode = mainLightNode,
            childNodes = listOfNotNull(modelNode)
        )
    }
}
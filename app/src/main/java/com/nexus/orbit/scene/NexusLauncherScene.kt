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

    // Key front light: natural, balanced studio illumination
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 155_000.0f
        position = Position(x = 0.0f, y = 3.2f, z = 3.2f)
        rotation = Rotation(x = -26.0f, y = 0.0f, z = 0.0f)
    }

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0.0f, y = 1.05f, z = 2.50f)
        rotation = Rotation(x = 0.0f, y = 0.0f, z = 0.0f)
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
                    scaleToUnits = 1.80f
                ).apply {
                    position = Position(x = 0f, y = state.avatarOffsetY, z = 0f)
                    scale = Scale(state.avatarScale)
                }
                modelNode = node

                val controller = AvatarRigController(
                    engine = engine,
                    asset = modelInstance.asset,
                    animator = modelInstance.animator
                )
                onRigControllerReady(controller)

                controller.updateCustomPose(
                    rX = state.rightArmX,
                    rY = state.rightArmY,
                    rZ = state.rightArmZ,
                    lX = state.leftArmX,
                    lY = state.leftArmY,
                    lZ = state.leftArmZ
                )
                controller.updateHeadPose(state.headPitch, state.headYaw)
            }
        } catch (e: Exception) {
            Log.e("NexusLauncherScene", "Error loading avatar GLB", e)
        }
    }

    LaunchedEffect(state.avatarOffsetY, state.avatarScale) {
        modelNode?.let { node ->
            node.position = Position(0f, state.avatarOffsetY, 0f)
            node.scale = Scale(state.avatarScale)
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
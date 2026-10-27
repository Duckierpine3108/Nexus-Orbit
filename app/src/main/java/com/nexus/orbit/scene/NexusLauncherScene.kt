package com.nexus.orbit.scene

import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.nexus.orbit.animation.AvatarAnimationController
import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.launcher.LauncherState
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.isActive

@Composable
fun NexusLauncherScene(
    state: LauncherState,
    settings: LauncherSettings,
    onRigControllerReady: (AvatarRigController) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    var rigController by remember { mutableStateOf<AvatarRigController?>(null) }
    var avatarNode by remember { mutableStateOf<ModelNode?>(null) }
    var assetMissing by remember { mutableStateOf(false) }

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0.0f, y = 1.25f, z = 3.6f)
        rotation = Rotation(x = -4.0f, y = 0.0f, z = 0.0f)
    }

    // Load actual GLB from mandatory asset path
    LaunchedEffect(Unit) {
        val modelPath = "models/nexus_avatar.glb"
        try {
            val assetExists = context.assets.list("models")?.contains("nexus_avatar.glb") == true
            if (!assetExists) {
                assetMissing = true
                return@LaunchedEffect
            }

            val modelInstance = modelLoader.createModelInstance(modelPath)
            if (modelInstance != null) {
                val node = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 1.78f // Normalizes avatar height to human proportions
                ).apply {
                    position = Position(0f, 0f, 0f)
                }
                avatarNode = node

                val controller = AvatarRigController(
                    engine = engine,
                    asset = modelInstance.asset,
                    animator = modelInstance.animator
                )
                rigController = controller
                onRigControllerReady(controller)

                // Initialize neutral reference pose (arms lowered)
                controller.applyPose(com.nexus.orbit.animation.AvatarPose.NEUTRAL_IDLE)
            }
        } catch (e: Exception) {
            assetMissing = true
        }
    }

    // Idle rotation tick
    LaunchedEffect(state.isDragging, state.isLaunching, settings.idleRotation) {
        while (isActive) {
            if (!state.isDragging && !state.isLaunching && settings.idleRotation) {
                state.ringAngle = (state.ringAngle + 0.0035f * settings.orbitSpeed) % (2f * Math.PI.toFloat())
            }
            kotlinx.coroutines.delay(16)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isLaunching) {
                if (state.isLaunching) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { state.isDragging = true },
                    onDragEnd = { state.isDragging = false },
                    onDragCancel = { state.isDragging = false },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val sensitivity = 0.0055f * settings.orbitSpeed
                        state.ringAngle -= dragAmount * sensitivity
                    }
                )
            }
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            childNodes = rememberNodes {
                avatarNode?.let { add(it) }
            }
        )
    }
}
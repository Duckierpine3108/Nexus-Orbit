package com.nexus.orbit.animation

import com.nexus.orbit.launcher.LauncherState
import com.nexus.orbit.scene.AvatarRigController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AvatarAnimationController(
    private val rigController: AvatarRigController?,
    private val state: LauncherState,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null

    fun resetToSavedNeutralPose() {
        rigController?.updateTiltAndPickPose(
            spineBendX = 0f,
            headPitch = state.headPitch,
            headYaw = state.headYaw,
            rUpperX = state.rightArmX, rUpperY = state.rightArmY, rUpperZ = state.rightArmZ,
            rForearmBend = 0f,
            lUpperX = state.leftArmX, lUpperY = state.leftArmY, lUpperZ = state.leftArmZ,
            lForearmBend = 0f
        )
        state.iconStretchX = 1.0f
        state.iconStretchY = 1.0f
        state.iconStretchAlpha = 1.0f
    }

    fun executeAppLaunchSequence(
        speedMultiplier: Float = 1.0f,
        onGrabTriggered: () -> Unit,
        onSequenceCompleted: () -> Unit
    ) {
        activeJob?.cancel()
        activeJob = scope.launch {
            val speed = speedMultiplier.coerceIn(0.5f, 2.5f)
            val stepDelay = 16L

            val startRx = state.rightArmX
            val startRy = state.rightArmY
            val startRz = state.rightArmZ

            val startLx = state.leftArmX
            val startLy = state.leftArmY
            val startLz = state.leftArmZ

            val baseHeadPitch = state.headPitch

            // PHASE 1: BEND FORWARD TO PICK APP (0ms -> 180ms)
            // Spine tilts forward 10°, head looks down at the app, hands subtly dip toward app
            val pickSteps = ((180f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..pickSteps) {
                val t = i.toFloat() / pickSteps.toFloat()
                val eased = 1f - (1f - t) * (1f - t)

                val spineBend = 10f * eased
                val headLookDown = baseHeadPitch + (12f * eased)

                // Hands dip forward naturally with body bend
                val curRx = startRx - (12f * eased)
                val curLx = startLx - (12f * eased)
                val forearmDip = 20f * eased

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineBend,
                    headPitch = headLookDown,
                    headYaw = state.headYaw,
                    rUpperX = curRx, rUpperY = startRy, rUpperZ = startRz,
                    rForearmBend = forearmDip,
                    lUpperX = curLx, lUpperY = startLy, lUpperZ = startLz,
                    lForearmBend = forearmDip
                )

                // Icon squeezes slightly as it's picked
                state.iconStretchX = 1.0f - (0.08f * eased)
                state.iconStretchY = 1.0f - (0.08f * eased)

                delay(stepDelay)
            }

            // PHASE 2: TRIGGER SYSTEM APP LAUNCH WINDOW
            onGrabTriggered()

            // PHASE 3: STRETCH ICON (180ms -> 340ms)
            // Icon physically stretches wide and expands directly into the opened window!
            val stretchSteps = ((160f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..stretchSteps) {
                val t = i.toFloat() / stretchSteps.toFloat()
                val eased = sin(t * (PI.toFloat() / 2f))

                // VISUAL STRETCH on the icon
                state.iconStretchX = 0.92f + (1.68f * eased) // Stretches to 2.6x
                state.iconStretchY = 0.92f + (0.88f * eased) // Stretches to 1.8x
                state.iconStretchAlpha = 1.0f - (0.45f * eased)

                delay(stepDelay)
            }

            // PHASE 4: SMOOTH UNBEND & RECOVERY TO UPRIGHT NEUTRAL (340ms -> 560ms)
            val recoverSteps = ((220f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..recoverSteps) {
                val t = i.toFloat() / recoverSteps.toFloat()
                val invT = 1f - t
                val eased = 1f - invT * invT * invT

                val spineBend = 10f * (1f - eased)
                val headLookDown = (baseHeadPitch + 12f) - (12f * eased)
                val curRx = (startRx - 12f) + (12f * eased)
                val curLx = (startLx - 12f) + (12f * eased)
                val forearmDip = 20f * (1f - eased)

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineBend,
                    headPitch = headLookDown,
                    headYaw = state.headYaw,
                    rUpperX = curRx, rUpperY = startRy, rUpperZ = startRz,
                    rForearmBend = forearmDip,
                    lUpperX = curLx, lUpperY = startLy, lUpperZ = startLz,
                    lForearmBend = forearmDip
                )

                state.iconStretchX = 2.60f - (1.60f * eased)
                state.iconStretchY = 1.80f - (0.80f * eased)
                state.iconStretchAlpha = 0.55f + (0.45f * eased)

                delay(stepDelay)
            }

            resetToSavedNeutralPose()
            onSequenceCompleted()
        }
    }

    fun cancelAndReset() {
        activeJob?.cancel()
        resetToSavedNeutralPose()
    }
}
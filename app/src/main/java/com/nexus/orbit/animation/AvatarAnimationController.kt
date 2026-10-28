package com.nexus.orbit.animation

import com.nexus.orbit.launcher.LauncherState
import com.nexus.orbit.scene.AvatarRigController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

            val startLx = state.leftArmX
            val startLy = state.leftArmY
            val startLz = state.leftArmZ

            val rightX = state.rightArmX
            val rightY = state.rightArmY
            val rightZ = state.rightArmZ

            val baseHeadPitch = state.headPitch

            // LEFT HAND PUSH TRAJECTORY:
            // Left Upper Arm raises forward (Pitch: 32°) and swings inward toward center (Yaw: -24°)
            // Left Forearm bends 70° forward
            // The left hand travels directly from his hip and pushes the center app icon!
            val pushUpperX = 32f
            val pushUpperY = -24f
            val pushUpperZ = 15f
            val pushForearmBend = 70f

            // PHASE 1: LEFT HAND PUSHES FORWARD INTO THE APP ICON (0ms -> 150ms)
            val pushSteps = ((150f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..pushSteps) {
                val t = i.toFloat() / pushSteps.toFloat()
                // Fast-in push curve
                val eased = 1f - (1f - t) * (1f - t) * (1f - t)

                val curLx = startLx + (pushUpperX - startLx) * eased
                val curLy = startLy + (pushUpperY - startLy) * eased
                val curLz = startLz + (pushUpperZ - startLz) * eased
                val curForearm = pushForearmBend * eased

                val spineTilt = 5f * eased
                val headLook = baseHeadPitch + (10f * eased)

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineTilt,
                    headPitch = headLook,
                    headYaw = state.headYaw,
                    rUpperX = rightX, rUpperY = rightY, rUpperZ = rightZ,
                    rForearmBend = 0f,
                    lUpperX = curLx, lUpperY = curLy, lUpperZ = curLz,
                    lForearmBend = curForearm
                )
                delay(stepDelay)
            }

            // PHASE 2: IMPACT! (At 150ms) -> Triggers Android app launch right as hand presses icon
            onGrabTriggered()

            // Brief 80ms follow-through press
            delay((80f / speed).toLong())

            // PHASE 3: LEFT HAND SMOOTHLY RETRACTS BACK TO HIS SIDE (230ms -> 460ms)
            val returnSteps = ((230f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..returnSteps) {
                val t = i.toFloat() / returnSteps.toFloat()
                val invT = 1f - t
                val eased = 1f - invT * invT

                val curLx = pushUpperX + (startLx - pushUpperX) * eased
                val curLy = pushUpperY + (startLy - pushUpperY) * eased
                val curLz = pushUpperZ + (startLz - pushUpperZ) * eased
                val curForearm = pushForearmBend * (1f - eased)

                val spineTilt = 5f * (1f - eased)
                val headLook = (baseHeadPitch + 10f) - (10f * eased)

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineTilt,
                    headPitch = headLook,
                    headYaw = state.headYaw,
                    rUpperX = rightX, rUpperY = rightY, rUpperZ = rightZ,
                    rForearmBend = 0f,
                    lUpperX = curLx, lUpperY = curLy, lUpperZ = curLz,
                    lForearmBend = curForearm
                )
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
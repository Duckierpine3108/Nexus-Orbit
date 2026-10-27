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
        rigController?.updateCustomPose(
            rX = state.rightArmX,
            rY = state.rightArmY,
            rZ = state.rightArmZ,
            lX = state.leftArmX,
            lY = state.leftArmY,
            lZ = state.leftArmZ
        )
        rigController?.updateHeadPose(state.headPitch, state.headYaw)
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

            val leftX = state.leftArmX
            val leftY = state.leftArmY
            val leftZ = state.leftArmZ

            val baseHeadPitch = state.headPitch

            // Target pose for Right Arm reaching directly onto front icon:
            val grabRx = 48f
            val grabRy = 8f
            val grabRz = -18f

            // PHASE 1: REACH & CLASP (0ms -> 180ms)
            val reachSteps = (180f / speed / stepDelay.toFloat()).toInt().coerceAtLeast(1)
            for (i in 1..reachSteps) {
                val t = i.toFloat() / reachSteps
                val eased = 1f - Math.pow((1.0 - t), 2.0).toFloat()

                val curRx = startRx + (grabRx - startRx) * eased
                val curRy = startRy + (grabRy - startRy) * eased
                val curRz = startRz + (grabRz - startRz) * eased

                rigController?.updateHeadPose(baseHeadPitch + 8f * eased, state.headYaw)

                rigController?.updateCustomPose(
                    rX = curRx, rY = curRy, rZ = curRz,
                    lX = leftX, lY = leftY, lZ = leftZ
                )

                state.iconStretchX = 1.0f - (0.08f * eased)
                state.iconStretchY = 1.0f - (0.08f * eased)

                delay(stepDelay)
            }

            // PHASE 2: TRIGGER SYSTEM APP LAUNCH
            onGrabTriggered()

            // PHASE 3: THE STRETCH (180ms -> 340ms)
            val stretchSteps = (160f / speed / stepDelay.toFloat()).toInt().coerceAtLeast(1)
            for (i in 1..stretchSteps) {
                val t = i.toFloat() / stretchSteps
                val eased = Math.sin(t * Math.PI / 2.0).toFloat()

                val stretchRx = grabRx + 6f * eased
                val stretchRy = grabRy + 12f * eased
                val stretchRz = grabRz - 6f * eased

                rigController?.updateCustomPose(
                    rX = stretchRx, rY = stretchRy, rZ = stretchRz,
                    lX = leftX, lY = leftY, lZ = leftZ
                )

                state.iconStretchX = 0.92f + (1.60f * eased)
                state.iconStretchY = 0.92f + (0.90f * eased)
                state.iconStretchAlpha = 1.0f - (0.40f * eased)

                delay(stepDelay)
            }

            // PHASE 4: RECOVERY BACK TO NEUTRAL (340ms -> 560ms)
            val recoverSteps = (220f / speed / stepDelay.toFloat()).toInt().coerceAtLeast(1)
            for (i in 1..recoverSteps) {
                val t = i.toFloat() / recoverSteps
                val eased = 1f - Math.pow((1.0 - t), 3.0).toFloat()

                val curRx = (grabRx + 6f) + (startRx - (grabRx + 6f)) * eased
                val curRy = (grabRy + 12f) + (startRy - (grabRy + 12f)) * eased
                val curRz = (grabRz - 6f) + (startRz - (grabRz - 6f)) * eased

                rigController?.updateHeadPose((baseHeadPitch + 8f) - (8f * eased), state.headYaw)

                rigController?.updateCustomPose(
                    rX = curRx, rY = curRy, rZ = curRz,
                    lX = leftX, lY = leftY, lZ = leftZ
                )

                state.iconStretchX = 2.52f - (1.52f * eased)
                state.iconStretchY = 1.82f - (0.82f * eased)
                state.iconStretchAlpha = 0.60f + (0.40f * eased)

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
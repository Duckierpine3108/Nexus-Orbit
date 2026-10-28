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
    }

    fun executeAppLaunchSequence(
        speedMultiplier: Float = 1.0f,
        onGrabTriggered: () -> Unit,
        onSequenceCompleted: () -> Unit
    ) {
        activeJob?.cancel()
        activeJob = scope.launch {
            // 1. Immediately launch the real Android app on tap!
            onGrabTriggered()

            val speed = speedMultiplier.coerceIn(0.5f, 2.5f)
            val stepDelay = 16L
            val baseHeadPitch = state.headPitch

            // Subtle body acknowledgement: avatar slightly tilts forward toward the tapped app
            val forwardSteps = ((120f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..forwardSteps) {
                val t = i.toFloat() / forwardSteps.toFloat()
                val eased = 1f - (1f - t) * (1f - t)

                val spineTilt = 6f * eased
                val headNod = baseHeadPitch + (8f * eased)

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineTilt,
                    headPitch = headNod,
                    headYaw = state.headYaw,
                    rUpperX = state.rightArmX, rUpperY = state.rightArmY, rUpperZ = state.rightArmZ,
                    rForearmBend = 0f,
                    lUpperX = state.leftArmX, lUpperY = state.leftArmY, lUpperZ = state.leftArmZ,
                    lForearmBend = 0f
                )
                delay(stepDelay)
            }

            // Smoothly returns upright
            val returnSteps = ((140f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..returnSteps) {
                val t = i.toFloat() / returnSteps.toFloat()
                val invT = 1f - t
                val eased = 1f - invT * invT

                val spineTilt = 6f * (1f - eased)
                val headNod = (baseHeadPitch + 8f) - (8f * eased)

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineTilt,
                    headPitch = headNod,
                    headYaw = state.headYaw,
                    rUpperX = state.rightArmX, rUpperY = state.rightArmY, rUpperZ = state.rightArmZ,
                    rForearmBend = 0f,
                    lUpperX = state.leftArmX, lUpperY = state.leftArmY, lUpperZ = state.leftArmZ,
                    lForearmBend = 0f
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
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

            // Left arm is locked 100% still in your pose
            val leftX = state.leftArmX
            val leftY = state.leftArmY
            val leftZ = state.leftArmZ

            // Right arm reach coordinates (smooth extension toward front app)
            val reachRx = 45f
            val reachRy = 5f
            val reachRz = -15f

            // 1. NEUTRAL -> REACH (~180ms)
            animateRightArm(
                fromX = startRx, fromY = startRy, fromZ = startRz,
                toX = reachRx, toY = reachRy, toZ = reachRz,
                leftX = leftX, leftY = leftY, leftZ = leftZ,
                durationMs = (180 / speed).toLong(),
                stepDelay = stepDelay
            )

            // 2. Trigger real Android App Launch
            onGrabTriggered()

            // 3. Subtle pulse (~100ms)
            delay((100 / speed).toLong())

            // 4. Smooth recovery back to your calibrated neutral numbers (~220ms)
            animateRightArm(
                fromX = reachRx, fromY = reachRy, fromZ = reachRz,
                toX = startRx, toY = startRy, toZ = startRz,
                leftX = leftX, leftY = leftY, leftZ = leftZ,
                durationMs = (220 / speed).toLong(),
                stepDelay = stepDelay
            )

            // Final guarantee: strictly restore user pose
            resetToSavedNeutralPose()
            onSequenceCompleted()
        }
    }

    private suspend fun animateRightArm(
        fromX: Float, fromY: Float, fromZ: Float,
        toX: Float, toY: Float, toZ: Float,
        leftX: Float, leftY: Float, leftZ: Float,
        durationMs: Long,
        stepDelay: Long
    ) {
        val steps = (durationMs / stepDelay).coerceAtLeast(1)
        for (i in 1..steps) {
            val fraction = i.toFloat() / steps
            val t = 1f - Math.pow((1.0 - fraction), 3.0).toFloat()

            val curRx = fromX + (toX - fromX) * t
            val curRy = fromY + (toY - fromY) * t
            val curRz = fromZ + (toZ - fromZ) * t

            rigController?.updateCustomPose(
                rX = curRx, rY = curRy, rZ = curRz,
                lX = leftX, lY = leftY, lZ = leftZ
            )
            delay(stepDelay)
        }
    }

    fun cancelAndReset() {
        activeJob?.cancel()
        resetToSavedNeutralPose()
    }
}
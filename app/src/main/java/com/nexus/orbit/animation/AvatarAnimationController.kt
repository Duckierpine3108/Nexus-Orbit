package com.nexus.orbit.animation

import com.nexus.orbit.launcher.LauncherSettings
import com.nexus.orbit.scene.AvatarRigController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AvatarAnimationController(
    private val rigController: AvatarRigController?,
    private val settings: LauncherSettings,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null

    fun resetToSavedNeutralPose() {
        rigController?.updateCustomPose(
            rX = settings.rightArmX,
            rY = settings.rightArmY,
            rZ = settings.rightArmZ,
            lX = settings.leftArmX,
            lY = settings.leftArmY,
            lZ = settings.leftArmZ
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

            val startRx = settings.rightArmX
            val startRy = settings.rightArmY
            val startRz = settings.rightArmZ

            // Left arm stays completely locked in your calibrated pose
            val leftX = settings.leftArmX
            val leftY = settings.leftArmY
            val leftZ = settings.leftArmZ

            // Right arm reach coordinates (smooth extension forward toward app)
            val reachRx = 38f
            val reachRy = 8f
            val reachRz = -12f

            // 1. NEUTRAL -> REACH (~190ms)
            animateRightArm(
                fromX = startRx, fromY = startRy, fromZ = startRz,
                toX = reachRx, toY = reachRy, toZ = reachRz,
                leftX = leftX, leftY = leftY, leftZ = leftZ,
                durationMs = (190 / speed).toLong(),
                stepDelay = stepDelay
            )

            // 2. Trigger real Android App Launch immediately
            onGrabTriggered()

            // 3. Subtle tap/pull extension (~110ms)
            animateRightArm(
                fromX = reachRx, fromY = reachRy, fromZ = reachRz,
                toX = reachRx + 6f, toY = reachRy + 4f, toZ = reachRz - 4f,
                leftX = leftX, leftY = leftY, leftZ = leftZ,
                durationMs = (110 / speed).toLong(),
                stepDelay = stepDelay
            )

            // 4. Smooth recovery back to your EXACT neutral pose (~240ms)
            animateRightArm(
                fromX = reachRx + 6f, fromY = reachRy + 4f, fromZ = reachRz - 4f,
                toX = startRx, toY = startRy, toZ = startRz,
                leftX = leftX, leftY = leftY, leftZ = leftZ,
                durationMs = (240 / speed).toLong(),
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
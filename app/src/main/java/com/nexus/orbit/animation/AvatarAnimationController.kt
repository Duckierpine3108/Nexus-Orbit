package com.nexus.orbit.animation

import com.nexus.orbit.scene.AvatarRigController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AvatarAnimationController(
    private val rigController: AvatarRigController?,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null
    var currentPose: AvatarPose = AvatarPose.NEUTRAL_IDLE
        private set

    fun initializeNeutralPose() {
        currentPose = AvatarPose.NEUTRAL_IDLE
        rigController?.applyPose(currentPose)
    }

    fun executeAppLaunchSequence(
        speedMultiplier: Float = 1.0f,
        onGrabTriggered: () -> Unit,
        onSequenceCompleted: () -> Unit
    ) {
        activeJob?.cancel()
        activeJob = scope.launch {
            val stepDelay = 16L
            val speed = speedMultiplier.coerceIn(0.5f, 2.5f)

            // 1. NEUTRAL -> REACH (~180ms)
            animateBetweenPoses(
                from = AvatarPose.NEUTRAL_IDLE,
                to = AvatarPose.REACH,
                durationMs = (180 / speed).toLong(),
                stepDelay = stepDelay
            )

            // 2. REACH -> GRAB (~100ms)
            animateBetweenPoses(
                from = AvatarPose.REACH,
                to = AvatarPose.GRAB,
                durationMs = (100 / speed).toLong(),
                stepDelay = stepDelay
            )

            // 3. Trigger immediate OS launch at grasp point
            onGrabTriggered()

            // 4. GRAB -> STRETCH (~160ms) (Accompanies window expansion)
            animateBetweenPoses(
                from = AvatarPose.GRAB,
                to = AvatarPose.STRETCH,
                durationMs = (160 / speed).toLong(),
                stepDelay = stepDelay
            )

            // Brief hold during launch handoff
            delay((60 / speed).toLong())

            // 5. STRETCH -> RECOVERY to NEUTRAL (~300ms)
            animateBetweenPoses(
                from = AvatarPose.STRETCH,
                to = AvatarPose.NEUTRAL_IDLE,
                durationMs = (300 / speed).toLong(),
                stepDelay = stepDelay
            )

            currentPose = AvatarPose.NEUTRAL_IDLE
            onSequenceCompleted()
        }
    }

    private suspend fun animateBetweenPoses(
        from: AvatarPose,
        to: AvatarPose,
        durationMs: Long,
        stepDelay: Long
    ) {
        val totalSteps = (durationMs / stepDelay).coerceAtLeast(1)
        for (step in 1..totalSteps) {
            val fraction = step.toFloat() / totalSteps
            // Smooth ease-out cubic
            val eased = 1f - Math.pow((1.0 - fraction), 3.0).toFloat()
            val blended = AvatarPose.lerp(from, to, eased)
            currentPose = blended
            rigController?.applyPose(blended)
            delay(stepDelay)
        }
    }

    fun cancelAndReset() {
        activeJob?.cancel()
        initializeNeutralPose()
    }
}
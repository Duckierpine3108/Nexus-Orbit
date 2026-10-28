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

            // FULL INWARD REACH COORDINATES:
            // Swings upper arm forward and inward across the chest, with an 82° elbow bend
            // This brings the hand directly from the hip onto the DEAD CENTER app icon!
            val targetUpperX = 32f
            val targetUpperY = 34f
            val targetUpperZ = -14f
            val targetForearmBend = 82f

            // PHASE 1: REACH & TOUCH CENTER APP (0ms -> 200ms)
            val reachSteps = ((200f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..reachSteps) {
                val t = i.toFloat() / reachSteps.toFloat()
                // Smooth cubic ease-out
                val eased = 1f - (1f - t) * (1f - t) * (1f - t)

                val spineBend = 8f * eased
                val headLook = baseHeadPitch + (14f * eased)

                // Right arm travels from hip all the way to center icon
                val curUpperX = startRx + (targetUpperX - startRx) * eased
                val curUpperY = startRy + (targetUpperY - startRy) * eased
                val curUpperZ = startRz + (targetUpperZ - startRz) * eased
                val curForearm = targetForearmBend * eased

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineBend,
                    headPitch = headLook,
                    headYaw = state.headYaw,
                    rUpperX = curUpperX, rUpperY = curUpperY, rUpperZ = curUpperZ,
                    rForearmBend = curForearm,
                    lUpperX = startLx, lUpperY = startLy, lUpperZ = startLz,
                    lForearmBend = 0f
                )

                // Icon compresses slightly under hand contact
                state.iconStretchX = 1.0f - (0.12f * eased)
                state.iconStretchY = 1.0f - (0.12f * eased)

                delay(stepDelay)
            }

            // PHASE 2: TRIGGER REAL ANDROID APP LAUNCH
            onGrabTriggered()

            // PHASE 3: THE PHYSICAL STRETCH (200ms -> 370ms)
            // Hand pulls outward slightly, and the icon stretches horizontally and vertically!
            val stretchSteps = ((170f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..stretchSteps) {
                val t = i.toFloat() / stretchSteps.toFloat()
                val eased = sin(t * (PI.toFloat() / 2f))

                val curUpperX = targetUpperX + (10f * eased)
                val curUpperY = targetUpperY + (12f * eased)
                val curForearm = targetForearmBend - (12f * eased)

                rigController?.updateTiltAndPickPose(
                    spineBendX = 8f * (1f - 0.5f * eased),
                    headPitch = baseHeadPitch + 14f,
                    headYaw = state.headYaw,
                    rUpperX = curUpperX, rUpperY = curUpperY, rUpperZ = targetUpperZ,
                    rForearmBend = curForearm,
                    lUpperX = startLx, lUpperY = startLy, lUpperZ = startLz,
                    lForearmBend = 0f
                )

                // Dynamic icon stretch: pulls open like a portal
                state.iconStretchX = 0.88f + (1.75f * eased) // Expands to 2.6x
                state.iconStretchY = 0.88f + (0.85f * eased) // Expands to 1.7x
                state.iconStretchAlpha = 1.0f - (0.50f * eased)

                delay(stepDelay)
            }

            // PHASE 4: SMOOTH RETURN TO RESTING POSE (370ms -> 610ms)
            val recoverSteps = ((240f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..recoverSteps) {
                val t = i.toFloat() / recoverSteps.toFloat()
                val invT = 1f - t
                val eased = 1f - invT * invT * invT

                val spineBend = 4f * (1f - eased)
                val headLook = (baseHeadPitch + 14f) - (14f * eased)

                val curUpperX = (targetUpperX + 10f) + (startRx - (targetUpperX + 10f)) * eased
                val curUpperY = (targetUpperY + 12f) + (startRy - (targetUpperY + 12f)) * eased
                val curUpperZ = targetUpperZ + (startRz - targetUpperZ) * eased
                val curForearm = (targetForearmBend - 12f) * (1f - eased)

                rigController?.updateTiltAndPickPose(
                    spineBendX = spineBend,
                    headPitch = headLook,
                    headYaw = state.headYaw,
                    rUpperX = curUpperX, rUpperY = curUpperY, rUpperZ = curUpperZ,
                    rForearmBend = curForearm,
                    lUpperX = startLx, lUpperY = startLy, lUpperZ = startLz,
                    lForearmBend = 0f
                )

                state.iconStretchX = 2.63f - (1.63f * eased)
                state.iconStretchY = 1.73f - (0.73f * eased)
                state.iconStretchAlpha = 0.50f + (0.50f * eased)

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
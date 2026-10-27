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
        rigController?.updateFullArmPose(
            rUpperX = state.rightArmX, rUpperY = state.rightArmY, rUpperZ = state.rightArmZ,
            rForearmX = 0f, rForearmY = 0f, rForearmZ = 0f,
            lUpperX = state.leftArmX, lUpperY = state.leftArmY, lUpperZ = state.leftArmZ,
            lForearmX = 0f, lForearmY = 0f, lForearmZ = 0f
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

            val startLx = state.leftArmX
            val startLy = state.leftArmY
            val startLz = state.leftArmZ

            val baseHeadPitch = state.headPitch

            // Target pose: Both forearms bend forward/inward 80 degrees, landing hands directly on the center app!
            val grabRx = 45f; val grabRy = 20f; val grabRz = -10f
            val grabLx = 45f; val grabLy = -20f; val grabLz = 10f
            val targetForearmBend = 80f

            // PHASE 1: REACH & CLASP (0ms -> 190ms)
            // Both hands come in from both sides and clasp the center icon; head looks down at the app
            val reachSteps = ((190f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..reachSteps) {
                val t = i.toFloat() / reachSteps.toFloat()
                val eased = 1f - (1f - t) * (1f - t)

                val curRx = startRx + (grabRx - startRx) * eased
                val curRy = startRy + (grabRy - startRy) * eased
                val curRz = startRz + (grabRz - startRz) * eased

                val curLx = startLx + (grabLx - startLx) * eased
                val curLy = startLy + (grabLy - startLy) * eased
                val curLz = startLz + (grabLz - startLz) * eased

                val curForearm = targetForearmBend * eased

                // Head looks down at the center app (+14 degrees)
                rigController?.updateHeadPose(baseHeadPitch + 14f * eased, state.headYaw)

                rigController?.updateFullArmPose(
                    rUpperX = curRx, rUpperY = curRy, rUpperZ = curRz,
                    rForearmX = curForearm, rForearmY = 0f, rForearmZ = 0f,
                    lUpperX = curLx, lUpperY = curLy, lUpperZ = curLz,
                    lForearmX = curForearm, lForearmY = 0f, lForearmZ = 0f
                )

                // Icon squeezes slightly under his clasp
                state.iconStretchX = 1.0f - (0.10f * eased)
                state.iconStretchY = 1.0f - (0.10f * eased)

                delay(stepDelay)
            }

            // PHASE 2: TRIGGER SYSTEM APP LAUNCH WINDOW
            onGrabTriggered()

            // PHASE 3: THE PHYSICAL STRETCH (190ms -> 360ms)
            // Both hands pull outward to the sides, and the 3D app icon stretches wide open!
            val stretchSteps = ((170f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..stretchSteps) {
                val t = i.toFloat() / stretchSteps.toFloat()
                val eased = sin(t * (PI.toFloat() / 2f))

                // Hands pull outward
                val stretchRx = grabRx + 10f * eased
                val stretchRy = grabRy + 15f * eased
                val stretchLx = grabLx + 10f * eased
                val stretchLy = grabLy - 15f * eased
                val stretchForearm = targetForearmBend - (15f * eased)

                rigController?.updateFullArmPose(
                    rUpperX = stretchRx, rUpperY = stretchRy, rUpperZ = grabRz,
                    rForearmX = stretchForearm, rForearmY = 0f, rForearmZ = 0f,
                    lUpperX = stretchLx, lUpperY = stretchLy, lUpperZ = grabLz,
                    lForearmX = stretchForearm, lForearmY = 0f, lForearmZ = 0f
                )

                // Icon stretches outward wide!
                state.iconStretchX = 0.90f + (1.80f * eased) // Stretches to 2.7x
                state.iconStretchY = 0.90f + (0.90f * eased) // Stretches to 1.8x
                state.iconStretchAlpha = 1.0f - (0.50f * eased)

                delay(stepDelay)
            }

            // PHASE 4: SMOOTH RECOVERY BACK TO NEUTRAL (360ms -> 600ms)
            val recoverSteps = ((240f / speed) / stepDelay).toInt().coerceAtLeast(1)
            for (i in 1..recoverSteps) {
                val t = i.toFloat() / recoverSteps.toFloat()
                val invT = 1f - t
                val eased = 1f - invT * invT * invT

                val curRx = (grabRx + 10f) + (startRx - (grabRx + 10f)) * eased
                val curRy = (grabRy + 15f) + (startRy - (grabRy + 15f)) * eased
                val curLx = (grabLx + 10f) + (startLx - (grabLx + 10f)) * eased
                val curLy = (grabLy - 15f) + (startLy - (grabLy - 15f)) * eased
                val curForearm = (targetForearmBend - 15f) * (1f - eased)

                // Head looks back up at the user
                rigController?.updateHeadPose((baseHeadPitch + 14f) - (14f * eased), state.headYaw)

                rigController?.updateFullArmPose(
                    rUpperX = curRx, rUpperY = curRy, rUpperZ = startRz,
                    rForearmX = curForearm, rForearmY = 0f, rForearmZ = 0f,
                    lUpperX = curLx, lUpperY = curLy, lUpperZ = startLz,
                    lForearmX = curForearm, lForearmY = 0f, lForearmZ = 0f
                )

                state.iconStretchX = 2.70f - (1.70f * eased)
                state.iconStretchY = 1.80f - (0.80f * eased)
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
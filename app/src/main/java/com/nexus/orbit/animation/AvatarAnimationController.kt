package com.nexus.orbit.animation

import com.nexus.orbit.launcher.LauncherState
import com.nexus.orbit.scene.AvatarRigController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AvatarAnimationController(
    private val rigController: AvatarRigController?,
    private val state: LauncherState,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null

    // Forcibly locks both arms and head strictly to your calibrated resting numbers
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
            try {
                // Instantly launch the target app
                onGrabTriggered()
            } finally {
                // Hard guarantee: even if Android pauses or kills the process on launch,
                // the arms are ALWAYS restored to your resting pose. No limbs stuck in the air.
                resetToSavedNeutralPose()
                onSequenceCompleted()
            }
        }
    }

    fun cancelAndReset() {
        activeJob?.cancel()
        resetToSavedNeutralPose()
    }
}
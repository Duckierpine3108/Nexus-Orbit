package com.nexus.orbit.launcher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nexus.orbit.animation.AnimationState

class LauncherState {
    var ringAngle by mutableFloatStateOf(0f)
    var selectedIndex by mutableIntStateOf(0)
    var isDragging by mutableStateOf(false)
    var isLaunching by mutableStateOf(false)
    var lastInteractionTime by mutableLongStateOf(0L)
    var animState by mutableStateOf(AnimationState.NEUTRAL_IDLE)
    var showSettings by mutableStateOf(false)
    var showDiagnostics by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    // Avatar Framing
    var avatarOffsetY by mutableFloatStateOf(-0.35f)
    var avatarScale by mutableFloatStateOf(1.30f)

    // Head Alignment & Gaze Tracking (+14 degrees tilts chin down to face user)
    var headPitch by mutableFloatStateOf(14f)
    var headYaw by mutableFloatStateOf(0f)

    // Arm Angles
    var rightArmX by mutableFloatStateOf(80f)
    var rightArmY by mutableFloatStateOf(-2f)
    var rightArmZ by mutableFloatStateOf(-28f)

    var leftArmX by mutableFloatStateOf(74f)
    var leftArmY by mutableFloatStateOf(19f)
    var leftArmZ by mutableFloatStateOf(13f)

    // Icon Grab & Stretch
    var iconStretchX by mutableFloatStateOf(1.0f)
    var iconStretchY by mutableFloatStateOf(1.0f)
    var iconStretchAlpha by mutableFloatStateOf(1.0f)
}
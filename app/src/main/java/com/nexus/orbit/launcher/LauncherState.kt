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
}
package com.nexus.orbit.launcher

import android.content.ComponentName
import android.graphics.Bitmap

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Bitmap,
    val componentName: ComponentName
)
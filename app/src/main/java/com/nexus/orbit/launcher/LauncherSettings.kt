package com.nexus.orbit.launcher

import android.content.Context
import android.content.SharedPreferences

class LauncherSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_orbit_prefs", Context.MODE_PRIVATE)

    var orbitSpeed: Float
        get() = prefs.getFloat(KEY_ORBIT_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_ORBIT_SPEED, value).apply()

    var idleRotation: Boolean
        get() = prefs.getBoolean(KEY_IDLE_ROTATION, true)
        set(value) = prefs.edit().putBoolean(KEY_IDLE_ROTATION, value).apply()

    var pauseDurationSeconds: Int
        get() = prefs.getInt(KEY_PAUSE_DURATION, 5)
        set(value) = prefs.edit().putInt(KEY_PAUSE_DURATION, value).apply()

    var animSpeed: Float
        get() = prefs.getFloat(KEY_ANIM_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_ANIM_SPEED, value).apply()

    // Avatar Height & Scale Settings
    var avatarOffsetY: Float
        get() = prefs.getFloat("avatar_offset_y", -0.85f)
        set(value) = prefs.edit().putFloat("avatar_offset_y", value).apply()

    var avatarScale: Float
        get() = prefs.getFloat("avatar_scale", 1.80f)
        set(value) = prefs.edit().putFloat("avatar_scale", value).apply()

    // Arm Rotation Settings (degrees)
    var rightArmX: Float
        get() = prefs.getFloat("arm_r_x", 0f)
        set(value) = prefs.edit().putFloat("arm_r_x", value).apply()

    var rightArmY: Float
        get() = prefs.getFloat("arm_r_y", 0f)
        set(value) = prefs.edit().putFloat("arm_r_y", value).apply()

    var rightArmZ: Float
        get() = prefs.getFloat("arm_r_z", -75f) // Default lowered along side
        set(value) = prefs.edit().putFloat("arm_r_z", value).apply()

    var leftArmX: Float
        get() = prefs.getFloat("arm_l_x", 0f)
        set(value) = prefs.edit().putFloat("arm_l_x", value).apply()

    var leftArmY: Float
        get() = prefs.getFloat("arm_l_y", 0f)
        set(value) = prefs.edit().putFloat("arm_l_y", value).apply()

    var leftArmZ: Float
        get() = prefs.getFloat("arm_l_z", 75f) // Default lowered along side
        set(value) = prefs.edit().putFloat("arm_l_z", value).apply()

    fun resetDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ORBIT_SPEED = "orbit_speed"
        private const val KEY_IDLE_ROTATION = "idle_rotation"
        private const val KEY_PAUSE_DURATION = "pause_duration_seconds"
        private const val KEY_ANIM_SPEED = "anim_speed"
    }
}
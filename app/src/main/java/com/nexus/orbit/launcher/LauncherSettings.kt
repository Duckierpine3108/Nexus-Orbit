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

    var animSpeed: Float
        get() = prefs.getFloat(KEY_ANIM_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_ANIM_SPEED, value).apply()

    var roomBrightness: Float
        get() = prefs.getFloat(KEY_BRIGHTNESS, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_BRIGHTNESS, value).apply()

    fun getJointMapping(role: String, defaultName: String): String {
        return prefs.getString("joint_role_$role", defaultName) ?: defaultName
    }

    fun setJointMapping(role: String, boneName: String) {
        prefs.edit().putString("joint_role_$role", boneName).apply()
    }

    fun resetDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ORBIT_SPEED = "orbit_speed"
        private const val KEY_IDLE_ROTATION = "idle_rotation"
        private const val KEY_ANIM_SPEED = "anim_speed"
        private const val KEY_BRIGHTNESS = "room_brightness"
    }
}
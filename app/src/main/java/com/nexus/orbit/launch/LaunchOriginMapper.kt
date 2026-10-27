package com.nexus.orbit.launch

import android.graphics.Rect

object LaunchOriginMapper {
    /**
     * Translates 3D projected coordinates to exact screen pixels for Android Window animations.
     */
    fun computeScreenBounds(screenX: Float, screenY: Float, iconSizeDp: Float, density: Float): Rect {
        val sizePx = (iconSizeDp * density).toInt()
        val left = (screenX - sizePx / 2f).toInt()
        val top = (screenY - sizePx / 2f).toInt()
        return Rect(left, top, left + sizePx, top + sizePx)
    }
}
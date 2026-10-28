package com.nexus.orbit.scene

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

data class ProjectedAppItem(
    val index: Int,
    val relPos: Float,
    val screenX: Float,
    val screenY: Float,
    val scale: Float,
    val alpha: Float,
    val isFront: Boolean
)

class AppRingController {
    fun computeOrbit(
        totalApps: Int,
        ringAngle: Float,
        viewportWidth: Float,
        viewportHeight: Float,
        ringHeightRatio: Float = 0.50f // Aligns right at waist/hand level!
    ): List<ProjectedAppItem> {
        if (totalApps <= 0) return emptyList()

        val step = (2.0 * PI / totalApps).toFloat()
        val floatCenter = -ringAngle / step
        val baseIndex = floatCenter.roundToInt()

        val list = mutableListOf<ProjectedAppItem>()
        val centerY = viewportHeight * ringHeightRatio
        val horizontalSpread = viewportWidth * 0.44f

        for (slot in -3..3) {
            val appIndex = ((baseIndex + slot) % totalApps + totalApps) % totalApps
            val relPos = slot - (floatCenter - baseIndex)

            if (abs(relPos) > 2.6f) continue

            val u = (relPos / 2.6f).coerceIn(-1.0f, 1.0f)
            val screenX = (viewportWidth / 2f) + sin(u * (PI.toFloat() / 2f)) * horizontalSpread
            val screenY = centerY + (u * u * 45f)

            val absU = abs(u)
            val scale = (1.30f - absU * 0.60f).coerceIn(0.65f, 1.35f)
            val alpha = (1.0f - absU * 0.75f).coerceIn(0.15f, 1.0f)
            val isFront = abs(relPos) < 0.35f

            list.add(
                ProjectedAppItem(
                    index = appIndex,
                    relPos = relPos,
                    screenX = screenX,
                    screenY = screenY,
                    scale = scale,
                    alpha = alpha,
                    isFront = isFront
                )
            )
        }

        return list.sortedBy { abs(it.relPos) }.reversed()
    }

    fun findFrontIndex(totalApps: Int, currentAngle: Float): Int {
        if (totalApps <= 0) return 0
        val step = (2.0 * PI / totalApps).toFloat()
        val floatCenter = -currentAngle / step
        val nearest = floatCenter.roundToInt()
        return ((nearest % totalApps) + totalApps) % totalApps
    }

    fun calculateSnapAngle(totalApps: Int, currentAngle: Float): Float {
        if (totalApps <= 0) return currentAngle
        val step = (2.0 * PI / totalApps).toFloat()
        val floatCenter = -currentAngle / step
        val nearest = floatCenter.roundToInt()
        return -nearest * step
    }
}
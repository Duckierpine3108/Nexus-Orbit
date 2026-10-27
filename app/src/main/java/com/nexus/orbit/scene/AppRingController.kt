package com.nexus.orbit.scene

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class ProjectedAppItem(
    val index: Int,
    val angleDiff: Float,
    val screenX: Float,
    val screenY: Float,
    val scale: Float,
    val alpha: Float,
    val isFront: Boolean
)

class AppRingController {
    companion object {
        // Only render apps within ~80 degrees of the front center (culls the back half)
        private const val MAX_VISIBLE_ANGLE = 1.40f
    }

    fun computeOrbit(
        totalApps: Int,
        ringAngle: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): List<ProjectedAppItem> {
        if (totalApps <= 0) return emptyList()

        val step = (2.0 * PI / totalApps).toFloat()
        val list = mutableListOf<ProjectedAppItem>()

        // Position the carousel in the lower-middle half (60% down)
        // so the avatar's upper body and face are fully visible above it
        val centerY = viewportHeight * 0.60f
        val horizontalSpread = viewportWidth * 0.46f
        val verticalCurve = 90f

        for (i in 0 until totalApps) {
            val rawAngle = (ringAngle + i * step) % (2f * PI.toFloat())
            val normAngle = if (rawAngle < 0) rawAngle + (2f * PI.toFloat()) else rawAngle

            // Angle difference relative to front center (0 radians)
            val diff = when {
                normAngle > PI -> normAngle - (2f * PI.toFloat())
                else -> normAngle
            }

            // CULLING: Skip apps on the back half to eliminate overcrowding
            if (abs(diff) > MAX_VISIBLE_ANGLE) {
                continue
            }

            val cosDiff = cos(diff)
            val sinDiff = sin(diff)

            // Screen X: Wide spread across phone width
            val screenX = (viewportWidth / 2f) + (sinDiff * horizontalSpread)

            // Screen Y: Slight 3D downward curve towards the sides
            val screenY = centerY + ((1f - cosDiff) * verticalCurve)

            // Scale: Front selected app is 1.35x, sides scale down smoothly
            val scale = 0.65f + (0.70f * (cosDiff * cosDiff))

            // Alpha: Fades out naturally towards the screen edges
            val alphaFactor = ((cosDiff - cos(MAX_VISIBLE_ANGLE)) / (1f - cos(MAX_VISIBLE_ANGLE))).coerceIn(0f, 1f)
            val alpha = (0.25f + 0.75f * alphaFactor).coerceIn(0f, 1f)

            val isFront = abs(diff) < (step * 0.5f).coerceAtMost(0.20f)

            list.add(
                ProjectedAppItem(
                    index = i,
                    angleDiff = diff,
                    screenX = screenX,
                    screenY = screenY,
                    scale = scale,
                    alpha = alpha,
                    isFront = isFront
                )
            )
        }

        // Render outer items first so the center front item is drawn on top
        return list.sortedBy { abs(it.angleDiff) }.reversed()
    }

    fun findFrontIndex(totalApps: Int, currentAngle: Float): Int {
        if (totalApps <= 0) return 0
        val step = (2.0 * PI / totalApps).toFloat()
        var closestIdx = 0
        var minDiff = Float.MAX_VALUE

        for (i in 0 until totalApps) {
            val rawAngle = (currentAngle + i * step) % (2f * PI.toFloat())
            val normAngle = if (rawAngle < 0) rawAngle + (2f * PI.toFloat()) else rawAngle
            val diff = when {
                normAngle > PI -> abs(normAngle - (2f * PI.toFloat()))
                else -> normAngle
            }
            if (diff < minDiff) {
                minDiff = diff
                closestIdx = i
            }
        }
        return closestIdx
    }

    fun calculateSnapAngle(totalApps: Int, currentAngle: Float): Float {
        if (totalApps <= 0) return currentAngle
        val step = (2.0 * PI / totalApps).toFloat()
        val targetIndex = findFrontIndex(totalApps, currentAngle)
        return -targetIndex * step
    }
}
package com.nexus.orbit.scene

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class ProjectedAppItem(
    val index: Int,
    val angle: Float,
    val posX: Float,
    val posY: Float,
    val posZ: Float,
    val screenX: Float,
    val screenY: Float,
    val scale: Float,
    val alpha: Float,
    val isFront: Boolean
)

class AppRingController(
    private val radiusX: Float = 1.8f,
    private val radiusZ: Float = 2.2f,
    private val orbitCenterY: Float = 1.0f
) {
    /**
     * Mathematical projection of orbiting application ring onto display viewport.
     */
    fun computeOrbit(
        totalApps: Int,
        ringAngle: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): List<ProjectedAppItem> {
        if (totalApps <= 0) return emptyList()

        val step = (2.0 * PI / totalApps).toFloat()
        val fovFactor = viewportHeight * 0.85f

        val list = mutableListOf<ProjectedAppItem>()

        for (i in 0 until totalApps) {
            // Angle around the avatar's vertical axis
            val itemAngle = (ringAngle + i * step) % (2f * PI.toFloat())
            val normalizedAngle = if (itemAngle < 0) itemAngle + 2f * PI.toFloat() else itemAngle

            // 3D Orbital Coordinates
            val x3d = radiusX * sin(normalizedAngle)
            val z3d = radiusZ * cos(normalizedAngle)
            val y3d = orbitCenterY + 0.15f * cos(normalizedAngle)

            // Perspective Camera math (Camera at Z = 3.6, Y = 1.25)
            val camZ = 3.6f
            val camY = 1.25f
            val depth = (camZ - z3d).coerceAtLeast(0.4f)
            val scale = (fovFactor / depth) / 1000f

            val screenX = (viewportWidth / 2f) + (x3d * scale * 340f)
            val screenY = (viewportHeight / 2f) - ((y3d - camY) * scale * 340f)

            // Far items fade naturally to preserve cinematic focus
            val alpha = ((z3d + radiusZ) / (2f * radiusZ)).coerceIn(0.15f, 1.0f)
            val isFront = (z3d >= radiusZ * 0.94f)

            list.add(
                ProjectedAppItem(
                    index = i,
                    angle = normalizedAngle,
                    posX = x3d,
                    posY = y3d,
                    posZ = z3d,
                    screenX = screenX,
                    screenY = screenY,
                    scale = scale.coerceIn(0.5f, 1.55f),
                    alpha = alpha,
                    isFront = isFront
                )
            )
        }

        // Render back items first for correct depth occlusion
        return list.sortedBy { it.posZ }
    }

    fun findFrontIndex(totalApps: Int, currentAngle: Float): Int {
        if (totalApps <= 0) return 0
        val step = (2.0 * PI / totalApps).toFloat()
        var closestIdx = 0
        var minDiff = Float.MAX_VALUE

        for (i in 0 until totalApps) {
            val angle = (currentAngle + i * step) % (2f * PI.toFloat())
            val normalized = if (angle < 0) angle + 2f * PI.toFloat() else angle
            // Target is 0 radians (front position)
            val diff = Math.min(normalized, (2f * PI.toFloat() - normalized))
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
package com.nexus.orbit.animation

/**
 * Angles in degrees [rotX, rotY, rotZ] to apply relative to the GLB bind pose.
 */
data class JointTransform(
    val rotX: Float = 0f,
    val rotY: Float = 0f,
    val rotZ: Float = 0f
)

data class AvatarPose(
    val rightShoulder: JointTransform = JointTransform(),
    val rightUpperArm: JointTransform = JointTransform(),
    val rightForearm: JointTransform = JointTransform(),
    val rightHand: JointTransform = JointTransform(),
    val leftShoulder: JointTransform = JointTransform(),
    val leftUpperArm: JointTransform = JointTransform(),
    val leftForearm: JointTransform = JointTransform(),
    val spine: JointTransform = JointTransform()
) {
    companion object {
        /**
         * Neutral interactive pose:
         * Standard human avatars default to T/A-pose.
         * Arms are lowered naturally along the sides.
         */
        val NEUTRAL_IDLE = AvatarPose(
            rightShoulder = JointTransform(rotX = 0f, rotY = 0f, rotZ = -10f),
            rightUpperArm = JointTransform(rotX = 12f, rotY = 5f, rotZ = -68f),
            rightForearm  = JointTransform(rotX = 18f, rotY = 0f, rotZ = -10f),
            rightHand     = JointTransform(rotX = 0f, rotY = 0f, rotZ = 0f),
            leftShoulder  = JointTransform(rotX = 0f, rotY = 0f, rotZ = 10f),
            leftUpperArm  = JointTransform(rotX = 12f, rotY = -5f, rotZ = 68f),
            leftForearm   = JointTransform(rotX = 18f, rotY = 0f, rotZ = 10f),
            spine         = JointTransform(rotX = 0f, rotY = 0f, rotZ = 0f)
        )

        /**
         * Reach: Right arm rises and extends toward the front orbit anchor.
         */
        val REACH = AvatarPose(
            rightShoulder = JointTransform(rotX = -5f, rotY = 15f, rotZ = 0f),
            rightUpperArm = JointTransform(rotX = 50f, rotY = 25f, rotZ = -15f),
            rightForearm  = JointTransform(rotX = 35f, rotY = 10f, rotZ = 0f),
            rightHand     = JointTransform(rotX = 10f, rotY = 0f, rotZ = 10f),
            leftShoulder  = JointTransform(rotX = 0f, rotY = 0f, rotZ = 10f),
            leftUpperArm  = JointTransform(rotX = 12f, rotY = -5f, rotZ = 68f),
            leftForearm   = JointTransform(rotX = 18f, rotY = 0f, rotZ = 10f),
            spine         = JointTransform(rotX = 4f, rotY = 8f, rotZ = 0f)
        )

        /**
         * Grab: Hand aligns and grasps the selected object.
         */
        val GRAB = AvatarPose(
            rightShoulder = JointTransform(rotX = -5f, rotY = 18f, rotZ = 0f),
            rightUpperArm = JointTransform(rotX = 54f, rotY = 22f, rotZ = -10f),
            rightForearm  = JointTransform(rotX = 40f, rotY = 15f, rotZ = -5f),
            rightHand     = JointTransform(rotX = 25f, rotY = 5f, rotZ = 20f),
            leftShoulder  = JointTransform(rotX = 0f, rotY = 0f, rotZ = 10f),
            leftUpperArm  = JointTransform(rotX = 12f, rotY = -5f, rotZ = 68f),
            leftForearm   = JointTransform(rotX = 18f, rotY = 0f, rotZ = 10f),
            spine         = JointTransform(rotX = 5f, rotY = 10f, rotZ = 0f)
        )

        /**
         * Stretch: The arm pulls outward as the app expands.
         */
        val STRETCH = AvatarPose(
            rightShoulder = JointTransform(rotX = -10f, rotY = 25f, rotZ = 5f),
            rightUpperArm = JointTransform(rotX = 42f, rotY = 35f, rotZ = -5f),
            rightForearm  = JointTransform(rotX = 25f, rotY = 25f, rotZ = 15f),
            rightHand     = JointTransform(rotX = 15f, rotY = 15f, rotZ = 25f),
            leftShoulder  = JointTransform(rotX = 0f, rotY = 0f, rotZ = 10f),
            leftUpperArm  = JointTransform(rotX = 12f, rotY = -5f, rotZ = 68f),
            leftForearm   = JointTransform(rotX = 18f, rotY = 0f, rotZ = 10f),
            spine         = JointTransform(rotX = 8f, rotY = 14f, rotZ = -2f)
        )

        fun lerp(start: AvatarPose, end: AvatarPose, fraction: Float): AvatarPose {
            val t = fraction.coerceIn(0f, 1f)
            fun lerpJoint(a: JointTransform, b: JointTransform) = JointTransform(
                rotX = a.rotX + (b.rotX - a.rotX) * t,
                rotY = a.rotY + (b.rotY - a.rotY) * t,
                rotZ = a.rotZ + (b.rotZ - a.rotZ) * t
            )
            return AvatarPose(
                rightShoulder = lerpJoint(start.rightShoulder, end.rightShoulder),
                rightUpperArm = lerpJoint(start.rightUpperArm, end.rightUpperArm),
                rightForearm  = lerpJoint(start.rightForearm, end.rightForearm),
                rightHand     = lerpJoint(start.rightHand, end.rightHand),
                leftShoulder  = lerpJoint(start.leftShoulder, end.leftShoulder),
                leftUpperArm  = lerpJoint(start.leftUpperArm, end.leftUpperArm),
                leftForearm   = lerpJoint(start.leftForearm, end.leftForearm),
                spine         = lerpJoint(start.spine, end.spine)
            )
        }
    }
}
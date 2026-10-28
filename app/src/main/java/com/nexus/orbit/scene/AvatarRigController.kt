package com.nexus.orbit.scene

import android.opengl.Matrix
import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.gltfio.Animator
import com.google.android.filament.gltfio.FilamentAsset
import com.nexus.orbit.animation.AvatarPose
import com.nexus.orbit.animation.JointTransform

class AvatarRigController(
    private val engine: Engine,
    private val asset: FilamentAsset,
    private val animator: Animator?
) {
    private val tm = engine.transformManager
    val discoveredJoints = mutableMapOf<String, Int>()
    private val initialBindTransforms = mutableMapOf<Int, FloatArray>()

    var rightShoulderEntity: Int = 0
    var rightUpperArmEntity: Int = 0
    var rightForearmEntity: Int = 0
    var rightHandEntity: Int = 0

    var leftShoulderEntity: Int = 0
    var leftUpperArmEntity: Int = 0
    var leftForearmEntity: Int = 0
    var leftHandEntity: Int = 0

    var spineEntity: Int = 0
    var headEntity: Int = 0
    var neckEntity: Int = 0

    init {
        inspectRigAndEnumerateJoints()
        autoMapJointRoles()
    }

    private fun inspectRigAndEnumerateJoints() {
        discoveredJoints.clear()
        initialBindTransforms.clear()

        val entities = asset.entities
        for (entity in entities) {
            val name = asset.getName(entity)
            if (!name.isNullOrBlank()) {
                discoveredJoints[name] = entity
                val instance = tm.getInstance(entity)
                if (instance != 0) {
                    val mat = FloatArray(16)
                    tm.getTransform(instance, mat)
                    initialBindTransforms[entity] = mat
                }
            }
        }
        Log.d("AvatarRigController", "Discovered ${discoveredJoints.size} joints.")
    }

    private fun autoMapJointRoles() {
        fun findBoneEntity(vararg patterns: Regex): Int {
            for ((name, entity) in discoveredJoints) {
                val isMesh = name.startsWith("Wolf3D", ignoreCase = true) ||
                             name.contains("mesh", ignoreCase = true) ||
                             name.contains("body", ignoreCase = true) ||
                             name.contains("hair", ignoreCase = true) ||
                             name.contains("teeth", ignoreCase = true) ||
                             name.contains("eye", ignoreCase = true)
                if (isMesh) continue

                for (p in patterns) {
                    if (p.containsMatchIn(name)) return entity
                }
            }
            return 0
        }

        rightShoulderEntity = findBoneEntity(
            Regex("(?i)^.*right.*shoulder.*$"),
            Regex("(?i)^.*right.*clavicle.*$"),
            Regex("(?i)^.*clavicle.*r.*$")
        )
        rightUpperArmEntity = findBoneEntity(
            Regex("(?i)^.*right.*upperarm.*$"),
            Regex("(?i)^.*rightarm.*$"),
            Regex("(?i)^.*arm.*r.*$")
        )
        rightForearmEntity = findBoneEntity(
            Regex("(?i)^.*right.*forearm.*$"),
            Regex("(?i)^.*right.*lowerarm.*$"),
            Regex("(?i)^.*forearm.*r.*$")
        )
        rightHandEntity = findBoneEntity(
            Regex("(?i)^.*right.*hand.*$"),
            Regex("(?i)^.*right.*wrist.*$")
        )

        leftShoulderEntity = findBoneEntity(
            Regex("(?i)^.*left.*shoulder.*$"),
            Regex("(?i)^.*left.*clavicle.*$")
        )
        leftUpperArmEntity = findBoneEntity(
            Regex("(?i)^.*left.*upperarm.*$"),
            Regex("(?i)^.*leftarm.*$"),
            Regex("(?i)^.*arm.*l.*$")
        )
        leftForearmEntity = findBoneEntity(
            Regex("(?i)^.*left.*forearm.*$"),
            Regex("(?i)^.*left.*lowerarm.*$"),
            Regex("(?i)^.*forearm.*l.*$")
        )
        leftHandEntity = findBoneEntity(
            Regex("(?i)^.*left.*hand.*$")
        )

        spineEntity = findBoneEntity(
            Regex("(?i)^.*spine1.*$"),
            Regex("(?i)^.*spine.*$"),
            Regex("(?i)^.*chest.*$")
        )

        headEntity = findBoneEntity(
            Regex("(?i)^.*head$"),
            Regex("(?i)^.*mixamorig.*head$")
        )
        neckEntity = findBoneEntity(
            Regex("(?i)^.*neck.*$")
        )
    }

    fun updateHeadPose(pitch: Float, yaw: Float) {
        val target = if (headEntity != 0) headEntity else neckEntity
        if (target != 0) {
            applyJoint(target, JointTransform(rotX = pitch, rotY = yaw, rotZ = 0f))
            animator?.updateBoneMatrices()
        }
    }

    fun updateCustomPose(
        rX: Float, rY: Float, rZ: Float,
        lX: Float, lY: Float, lZ: Float
    ) {
        val targetRight = if (rightUpperArmEntity != 0) rightUpperArmEntity else rightShoulderEntity
        val targetLeft = if (leftUpperArmEntity != 0) leftUpperArmEntity else leftShoulderEntity

        if (targetRight != 0) applyJoint(targetRight, JointTransform(rX, rY, rZ))
        if (targetLeft != 0) applyJoint(targetLeft, JointTransform(lX, lY, lZ))
        animator?.updateBoneMatrices()
    }

    // Spine tilt + head look + subtle arm reach for picking up the app
    fun updateTiltAndPickPose(
        spineBendX: Float,
        headPitch: Float, headYaw: Float,
        rUpperX: Float, rUpperY: Float, rUpperZ: Float,
        rForearmBend: Float,
        lUpperX: Float, lUpperY: Float, lUpperZ: Float,
        lForearmBend: Float
    ) {
        if (spineEntity != 0) {
            applyJoint(spineEntity, JointTransform(rotX = spineBendX, rotY = 0f, rotZ = 0f))
        }

        val targetHead = if (headEntity != 0) headEntity else neckEntity
        if (targetHead != 0) {
            applyJoint(targetHead, JointTransform(rotX = headPitch, rotY = headYaw, rotZ = 0f))
        }

        val targetRight = if (rightUpperArmEntity != 0) rightUpperArmEntity else rightShoulderEntity
        val targetLeft = if (leftUpperArmEntity != 0) leftUpperArmEntity else leftShoulderEntity

        if (targetRight != 0) applyJoint(targetRight, JointTransform(rUpperX, rUpperY, rUpperZ))
        if (rightForearmEntity != 0) applyJoint(rightForearmEntity, JointTransform(rForearmBend, 0f, 0f))

        if (targetLeft != 0) applyJoint(targetLeft, JointTransform(lUpperX, lUpperY, lUpperZ))
        if (leftForearmEntity != 0) applyJoint(leftForearmEntity, JointTransform(lForearmBend, 0f, 0f))

        animator?.updateBoneMatrices()
    }

    fun applyDirectJointRotation(entity: Int, rotX: Float, rotY: Float, rotZ: Float) {
        applyJoint(entity, JointTransform(rotX, rotY, rotZ))
        animator?.updateBoneMatrices()
    }

    fun resetToBindPose() {
        for ((entity, bindMat) in initialBindTransforms) {
            val instance = tm.getInstance(entity)
            if (instance != 0) {
                tm.setTransform(instance, bindMat)
            }
        }
        animator?.updateBoneMatrices()
    }

    fun applyPose(pose: AvatarPose) {
        applyJoint(rightShoulderEntity, pose.rightShoulder)
        applyJoint(rightUpperArmEntity, pose.rightUpperArm)
        applyJoint(rightForearmEntity, pose.rightForearm)
        applyJoint(rightHandEntity, pose.rightHand)

        applyJoint(leftShoulderEntity, pose.leftShoulder)
        applyJoint(leftUpperArmEntity, pose.leftUpperArm)
        applyJoint(leftForearmEntity, pose.leftForearm)
        applyJoint(spineEntity, pose.spine)

        animator?.updateBoneMatrices()
    }

    private fun applyJoint(entity: Int, transform: JointTransform) {
        if (entity == 0) return
        val instance = tm.getInstance(entity)
        if (instance == 0) return

        val baseTransform = initialBindTransforms[entity] ?: return

        val deltaMatrix = FloatArray(16)
        Matrix.setIdentityM(deltaMatrix, 0)
        Matrix.rotateM(deltaMatrix, 0, transform.rotX, 1f, 0f, 0f)
        Matrix.rotateM(deltaMatrix, 0, transform.rotY, 0f, 1f, 0f)
        Matrix.rotateM(deltaMatrix, 0, transform.rotZ, 0f, 0f, 1f)

        val finalMatrix = FloatArray(16)
        Matrix.multiplyMM(finalMatrix, 0, baseTransform, 0, deltaMatrix, 0)

        tm.setTransform(instance, finalMatrix)
    }
}
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
        fun findEntity(vararg patterns: Regex): Int {
            for (p in patterns) {
                for ((name, entity) in discoveredJoints) {
                    if (p.containsMatchIn(name)) return entity
                }
            }
            return 0
        }

        rightShoulderEntity = findEntity(
            Regex("(?i).*(right|r_).*shoulder.*"),
            Regex("(?i).*(right|r_).*clavicle.*"),
            Regex("(?i).*clavicle.*r.*")
        )
        rightUpperArmEntity = findEntity(
            Regex("(?i).*(right|r_).*upperarm.*"),
            Regex("(?i).*(right|r_).*arm.*"),
            Regex("(?i).*arm.*r.*")
        )
        rightForearmEntity = findEntity(
            Regex("(?i).*(right|r_).*forearm.*"),
            Regex("(?i).*(right|r_).*lowerarm.*")
        )
        rightHandEntity = findEntity(
            Regex("(?i).*(right|r_).*hand.*"),
            Regex("(?i).*(right|r_).*wrist.*")
        )

        leftShoulderEntity = findEntity(
            Regex("(?i).*(left|l_).*shoulder.*"),
            Regex("(?i).*(left|l_).*clavicle.*")
        )
        leftUpperArmEntity = findEntity(
            Regex("(?i).*(left|l_).*upperarm.*"),
            Regex("(?i).*(left|l_).*arm.*"),
            Regex("(?i).*arm.*l.*")
        )
        leftForearmEntity = findEntity(
            Regex("(?i).*(left|l_).*forearm.*"),
            Regex("(?i).*(left|l_).*lowerarm.*")
        )
        leftHandEntity = findEntity(
            Regex("(?i).*(left|l_).*hand.*")
        )

        spineEntity = findEntity(
            Regex("(?i).*spine1.*"),
            Regex("(?i).*spine.*"),
            Regex("(?i).*chest.*")
        )

        headEntity = findEntity(
            Regex("(?i).*head.*")
        )
        neckEntity = findEntity(
            Regex("(?i).*neck.*")
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

        if (targetRight != 0) {
            applyJoint(targetRight, JointTransform(rX, rY, rZ))
        }
        if (targetLeft != 0) {
            applyJoint(targetLeft, JointTransform(lX, lY, lZ))
        }
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
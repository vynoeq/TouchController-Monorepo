package top.fifthlight.blazerod.common.resource

import org.joml.Vector3fc
import top.fifthlight.blazerod.model.PhysicalJoint

data class RenderPhysicsJoint(
    val name: String? = null,
    val type: top.fifthlight.blazerod.model.PhysicalJoint.JointType,
    val rigidBodyAIndex: Int,
    val rigidBodyBIndex: Int,
    val position: org.joml.Vector3fc,
    val rotation: org.joml.Vector3fc,
    val positionMin: org.joml.Vector3fc,
    val positionMax: org.joml.Vector3fc,
    val rotationMin: org.joml.Vector3fc,
    val rotationMax: org.joml.Vector3fc,
    val positionSpring: org.joml.Vector3fc,
    val rotationSpring: org.joml.Vector3fc,
)

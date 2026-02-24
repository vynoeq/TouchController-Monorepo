package top.fifthlight.blazerod.common.resource

import org.joml.*
import top.fifthlight.blazerod.render.api.resource.CameraTransform
import top.fifthlight.blazerod.model.Camera

sealed class CameraTransformImpl : top.fifthlight.blazerod.render.api.resource.CameraTransform {
    override val rotationQuaternion = org.joml.Quaternionf()
    override val rotationEulerAngles = org.joml.Vector3f()
    override val position = org.joml.Vector3f()

    companion object {
        fun of(camera: top.fifthlight.blazerod.model.Camera) = when (camera) {
            is top.fifthlight.blazerod.model.Camera.MMD -> MMD(
                targetPosition = org.joml.Vector3f(0f),
                fov = 70f,
                distance = 1f,
                rotationEulerAngles = org.joml.Vector3f(),
            )

            is top.fifthlight.blazerod.model.Camera.Perspective -> Perspective(
                yfov = camera.yfov,
                zfar = camera.zfar,
                znear = camera.znear
            )

            is top.fifthlight.blazerod.model.Camera.Orthographic -> Orthographic(
                xmag = camera.ymag,
                ymag = camera.xmag,
                zfar = camera.zfar,
                znear = camera.znear
            )
        }
    }

    class MMD(
        override val targetPosition: org.joml.Vector3f,
        override var fov: Float,
        override var distance: Float,
        rotationEulerAngles: org.joml.Vector3fc,
    ) : CameraTransformImpl(), top.fifthlight.blazerod.render.api.resource.CameraTransform.MMD {
        private val offsetCache = org.joml.Vector3f()

        init {
            this.rotationEulerAngles.set(rotationEulerAngles)
            update(org.joml.Matrix4f())
        }

        override fun update(matrix: org.joml.Matrix4fc) {
            rotationQuaternion.rotationZYX(
                this.rotationEulerAngles.z(),
                this.rotationEulerAngles.y(),
                this.rotationEulerAngles.x()
            )
            rotationQuaternion.normalize()
            val cameraLocalOffset = offsetCache.set(0f, 0f, -distance)
            val rotatedCameraOffset = rotationQuaternion.transform(cameraLocalOffset)
            matrix.getTranslation(position).add(targetPosition).add(rotatedCameraOffset)
        }

        override fun getMatrix(matrix: org.joml.Matrix4f, aspectRatio: Float, farPlaneDistance: Float) {
            matrix.perspective(fov, aspectRatio, 0.05f, farPlaneDistance)
        }
    }

    class Perspective(
        override var yfov: Float,
        override var zfar: Float? = null,
        override var znear: Float,
    ) : CameraTransformImpl(), top.fifthlight.blazerod.render.api.resource.CameraTransform.Perspective {
        override fun getMatrix(matrix: org.joml.Matrix4f, aspectRatio: Float, farPlaneDistance: Float) {
            matrix.perspective(yfov, aspectRatio, znear, zfar ?: farPlaneDistance)
        }
    }

    class Orthographic(
        override var xmag: Float,
        override var ymag: Float,
        override var zfar: Float,
        override var znear: Float,
    ) : CameraTransformImpl(), top.fifthlight.blazerod.render.api.resource.CameraTransform.Orthographic {
        override fun getMatrix(matrix: org.joml.Matrix4f, aspectRatio: Float, farPlaneDistance: Float) {
            val xmag = ymag * aspectRatio
            matrix.ortho(-xmag, xmag, -ymag, ymag, znear, zfar)
        }
    }

    open fun update(matrix: org.joml.Matrix4fc) {
        matrix.getTranslation(position)
        matrix.getUnnormalizedRotation(rotationQuaternion)
        rotationQuaternion.getEulerAnglesXYZ(rotationEulerAngles)
    }
}


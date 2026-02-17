package top.fifthlight.blazerod.render.version_1_21_8.util.math

import net.minecraft.world.phys.Vec3
import org.joml.Vector3d

fun Vector3d.set(vec3d: Vec3) = apply {
    x = vec3d.x
    y = vec3d.y
    z = vec3d.z
}

fun Vec3.sub(v: Vec3, dst: Vector3d) = dst.apply {
    x = this@sub.x - v.x
    y = this@sub.y - v.y
    z = this@sub.z - v.z
}

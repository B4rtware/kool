package de.fabmax.kool.editor.data

import de.fabmax.kool.math.*
import de.fabmax.kool.scene.Transform
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MutableColor
import kotlinx.serialization.Serializable

@Serializable
data class TransformData(
    val position: Vec3Data,
    val rotation: Vec4Data,
    val scale: Vec3Data
) {
    constructor(matrix: Mat4d): this(
        position = Vec3Data(matrix.getOrigin(MutableVec3d())),
        rotation = Vec4Data(matrix.getRotation(MutableVec4d())),
        scale = Vec3Data(matrix.getScale(MutableVec3d()))
    )

    fun toTransform(result: Transform): Transform {
        toMat4d(result.matrix)
        result.markDirty()
        return result
    }

    fun toMat4d(result: Mat4d = Mat4d()): Mat4d {
        return result
            .setRotate(rotation.toVec4d())
            .scale(scale.toVec3d())
            .setOrigin(position.toVec3d())
    }

    fun toMat4f(result: Mat4f = Mat4f()): Mat4f {
        return result
            .setRotate(rotation.toVec4f())
            .scale(scale.toVec3f())
            .setOrigin(position.toVec3f())
    }

    companion object {
        val IDENTITY = TransformData(
            position = Vec3Data(Vec3d.ZERO),
            rotation = Vec4Data(Vec4d.W_AXIS),
            scale = Vec3Data(Vec3d(1.0, 1.0, 1.0))
        )
    }
}

@Serializable
data class Vec2Data(val x: Double, val y: Double) {
    constructor(vec: Vec2d): this(vec.x, vec.y)
    constructor(vec: Vec2f): this(vec.x.toDouble(), vec.y.toDouble())
    constructor(vec: Vec2i): this(vec.x.toDouble(), vec.y.toDouble())

    fun toVec2f(result: MutableVec2f = MutableVec2f()): MutableVec2f {
        return result.set(x.toFloat(), y.toFloat())
    }

    fun toVec2d(result: MutableVec2d = MutableVec2d()): MutableVec2d {
        return result.set(x, y)
    }

    fun toVec2i(result: MutableVec2i = MutableVec2i()): MutableVec2i {
        return result.also { it.x = x.toInt(); it.y = y.toInt() }
    }
}

@Serializable
data class Vec3Data(val x: Double, val y: Double, val z: Double) {
    constructor(vec: Vec3d): this(vec.x, vec.y, vec.z)
    constructor(vec: Vec3f): this(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
    constructor(vec: Vec3i): this(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())

    fun toVec3f(result: MutableVec3f = MutableVec3f()): MutableVec3f {
        return result.set(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun toVec3d(result: MutableVec3d = MutableVec3d()): MutableVec3d {
        return result.set(x, y, z)
    }

    fun toVec3i(result: MutableVec3i = MutableVec3i()): MutableVec3i {
        return result.set(x.toInt(), y.toInt(), z.toInt())
    }
}

@Serializable
data class Vec4Data(val x: Double, val y: Double, val z: Double, val w: Double) {
    constructor(vec: Vec4d): this(vec.x, vec.y, vec.z, vec.w)
    constructor(vec: Vec4f): this(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble(), vec.w.toDouble())
    constructor(vec: Vec4i): this(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble(), vec.w.toDouble())

    fun toVec4f(result: MutableVec4f = MutableVec4f()): MutableVec4f {
        return result.set(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())
    }

    fun toVec4d(result: MutableVec4d = MutableVec4d()): MutableVec4d {
        return result.set(x, y, z, w)
    }

    fun toVec4i(result: MutableVec4i = MutableVec4i()): MutableVec4i {
        return result.set(x.toInt(), y.toInt(), z.toInt(), w.toInt())
    }
}

@Serializable
data class ColorData(val r: Float, val g: Float, val b: Float, val a: Float) {
    constructor(color: Color): this(color.r, color.g, color.b, color.a)

    fun toColor(result: MutableColor = MutableColor()): MutableColor {
        return result.set(r, g, b, a)
    }
}

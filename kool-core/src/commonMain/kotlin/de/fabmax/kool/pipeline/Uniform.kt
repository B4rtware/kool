package de.fabmax.kool.pipeline

import de.fabmax.kool.math.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MixedBuffer
import de.fabmax.kool.util.MutableColor

sealed class Uniform<T>(
    var value: T,
    val name: String,

    /**
     * Number of elements in case this is an array type (1 otherwise)
     */
    val length: Int = 1
) {

    val isArray: Boolean
        get() = length > 1

    /**
     * Appends this uniform's data to the supplied buffer at its current position. Does not check for alignment, i.e.
     * the buffer position needs to be correctly set before calling this method. Trailing padding bytes are appended
     * until the specified number of bytes [len] are appended.
     *
     * For array types, [len] is also used to decide whether to add intermediate padding (Std140 layout) or not.
     */
    abstract fun putToBuffer(buffer: MixedBuffer, len: Int)

    protected fun checkLen(minRequired: Int, available: Int) {
        if (available < minRequired) {
            error("Insufficient buffer space: $minRequired > $available")
        }
    }

    protected fun putPadding(buffer: MixedBuffer, padLen: Int) {
        if (padLen > 0) {
            buffer.padding(padLen)
        }
    }

    override fun toString(): String {
        return name
    }
}

class Uniform1f(name: String) : Uniform<Float>(0f, name) {
    constructor(initValue: Float, name: String) : this(name) {
        value = initValue
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(4, len)
        buffer.putFloat32(value)
        putPadding(buffer, len - 4)
    }
}

class Uniform2f(name: String) : Uniform<MutableVec2f>(MutableVec2f(), name) {
    constructor(initValue: Vec2f, name: String) : this(name) {
        value.set(initValue)
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(8, len)
        buffer.putFloat32(value.array)
        putPadding(buffer, len - 8)
    }
}

class Uniform3f(name: String) : Uniform<MutableVec3f>(MutableVec3f(), name) {
    constructor(initValue: Vec3f, name: String) : this(name) {
        value.set(initValue)
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(12, len)
        buffer.putFloat32(value.array)
        putPadding(buffer, len - 12)
    }
}

class Uniform4f(name: String) : Uniform<MutableVec4f>(MutableVec4f(), name) {
    constructor(initValue: Vec4f, name: String) : this(name) {
        value.set(initValue)
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(16, len)
        buffer.putFloat32(value.array)
        putPadding(buffer, len - 16)
    }
}

class UniformColor(name: String) : Uniform<MutableColor>(MutableColor(), name) {
    constructor(initValue: Color, name: String) : this(name) {
        value.set(initValue)
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(16, len)
        buffer.putFloat32(value.array)
        putPadding(buffer, len - 16)
    }
}

class Uniform1fv(name: String, length: Int) : Uniform<FloatArray>(FloatArray(length), name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(4 * length, len)
        val padLen = (len - 4 * length) / length
        for (i in 0 until length) {
            buffer.putFloat32(value[i])
            putPadding(buffer, padLen)
        }
    }
}

class Uniform2fv(name: String, length: Int) : Uniform<Array<MutableVec2f>>(Array(length) { MutableVec2f() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(8 * length, len)
        val padLen = (len - 8 * length) / length
        for (i in 0 until length) {
            buffer.putFloat32(value[i].array)
            putPadding(buffer, padLen)
        }
    }
}

class Uniform3fv(name: String, length: Int) : Uniform<Array<MutableVec3f>>(Array(length) { MutableVec3f() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(12 * length, len)
        val padLen = (len - 12 * length) / length
        for (i in 0 until length) {
            buffer.putFloat32(value[i].array)
            putPadding(buffer, padLen)
        }
    }
}

class Uniform4fv(name: String, length: Int) : Uniform<Array<MutableVec4f>>(Array(length) { MutableVec4f() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(16 * length, len)
        // Uniform4f arrays never contain padding
        for (i in 0 until length) {
            buffer.putFloat32(value[i].array)
        }
    }
}

class UniformMat3f(name: String) : Uniform<Mat3f>(Mat3f(), name) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(3 * 12, len)
        val padLen = (len - 3 * 12) / 3
        for (m in 0..2) {
            buffer.putFloat32(value.matrix, m * 3, 3)
            putPadding(buffer, padLen)
        }
    }
}

class UniformMat3fv(name: String, length: Int) : Uniform<Array<Mat3f>>(Array(length) { Mat3f() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(3 * 12 * length, len)
        val padLen = (len - 3 * 12 * length) / (3 * length)
        for (i in 0 until length) {
            for (m in 0..2) {
                buffer.putFloat32(value[i].matrix, m * 3, 3)
                putPadding(buffer, padLen)
            }
        }
    }
}

class UniformMat4f(name: String) : Uniform<Mat4f>(Mat4f(), name) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(4 * 16, len)
        buffer.putFloat32(value.array)
    }
}

class UniformMat4fv(name: String, length: Int) : Uniform<Array<Mat4f>>(Array(length) { Mat4f() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(4 * 16 * length, len)
        for (i in 0 until length) {
            buffer.putFloat32(value[i].array)
        }
    }
}

class Uniform1i(name: String) : Uniform<Int>(0, name) {
    constructor(initValue: Int, name: String) : this(name) {
        value = initValue
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(4, len)
        buffer.putInt32(value)
        putPadding(buffer, len - 4)
    }
}

class Uniform2i(name: String) : Uniform<MutableVec2i>(MutableVec2i(), name) {
    constructor(initValue: Vec2i, name: String) : this(name) {
        value.set(initValue)
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(8, len)
        buffer.putInt32(value.array)
        putPadding(buffer, len - 8)
    }
}

class Uniform3i(name: String) : Uniform<MutableVec3i>(MutableVec3i(), name) {
    constructor(initValue: Vec3i, name: String) : this(name) {
        value.set(initValue)
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(12, len)
        buffer.putInt32(value.array)
        putPadding(buffer, len - 12)
    }
}

class Uniform4i(name: String) : Uniform<MutableVec4i>(MutableVec4i(), name) {
    constructor(initValue: Vec4i, name: String) : this(name) {
        value.set(initValue)
    }

    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(16, len)
        buffer.putInt32(value.array)
        putPadding(buffer, len - 16)
    }
}

class Uniform1iv(name: String, length: Int) : Uniform<IntArray>(IntArray(length), name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(4 * length, len)
        val padLen = (len - 4 * length) / length
        for (i in 0 until length) {
            buffer.putInt32(value[i])
            putPadding(buffer, padLen)
        }
    }
}

class Uniform2iv(name: String, length: Int) : Uniform<Array<MutableVec2i>>(Array(length) { MutableVec2i() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(8 * length, len)
        val padLen = (len - 8 * length) / length
        for (i in 0 until length) {
            buffer.putInt32(value[i].array)
            putPadding(buffer, padLen)
        }
    }
}

class Uniform3iv(name: String, length: Int) : Uniform<Array<MutableVec3i>>(Array(length) { MutableVec3i() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(12 * length, len)
        val padLen = (len - 12 * length) / length
        for (i in 0 until length) {
            buffer.putInt32(value[i].array)
            putPadding(buffer, padLen)
        }
    }
}

class Uniform4iv(name: String, length: Int) : Uniform<Array<MutableVec4i>>(Array(length) { MutableVec4i() }, name, length) {
    override fun putToBuffer(buffer: MixedBuffer, len: Int) {
        checkLen(16 * length, len)
        for (i in 0 until length) {
            buffer.putInt32(value[i].array)
        }
    }
}
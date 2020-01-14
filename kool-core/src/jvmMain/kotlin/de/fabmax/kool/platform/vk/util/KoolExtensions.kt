package de.fabmax.kool.platform.vk.util

import de.fabmax.kool.gl.GL_ALPHA
import de.fabmax.kool.pipeline.ShaderStage
import de.fabmax.kool.pipeline.TexFormat
import org.lwjgl.opengl.GL11
import org.lwjgl.vulkan.VK10.*

fun ShaderStage.bitValue(): Int {
    return when (this) {
        ShaderStage.VERTEX_SHADER -> VK_SHADER_STAGE_VERTEX_BIT
        ShaderStage.TESSELEATION_CTRL -> VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT
        ShaderStage.TESSELATION_EVAL -> VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT
        ShaderStage.GEOMETRY_SHADER -> VK_SHADER_STAGE_GEOMETRY_BIT
        ShaderStage.FRAGMENT_SHADER -> VK_SHADER_STAGE_FRAGMENT_BIT
        ShaderStage.ALL -> VK_SHADER_STAGE_ALL
    }
}

val TexFormat.vkFormat: Int
    get() = when(this) {
        TexFormat.ALPHA -> VK_FORMAT_R8_UNORM
        TexFormat.RGB -> VK_FORMAT_R8G8B8_UNORM
        TexFormat.RGBA -> VK_FORMAT_R8G8B8A8_UNORM
        TexFormat.RG_F16 -> VK_FORMAT_R16G16_SFLOAT
        TexFormat.RGBA_F16 -> VK_FORMAT_R16G16B16A16_SFLOAT
    }

val TexFormat.vkBytesPerPx: Int
    get() = when(this) {
        TexFormat.ALPHA -> 1
        TexFormat.RGB -> 3
        TexFormat.RGBA -> 4
        TexFormat.RG_F16 -> 4
        TexFormat.RGBA_F16 -> 8
    }

val TexFormat.glFormat: Int
    get() = when(this) {
        TexFormat.ALPHA -> GL_ALPHA
        TexFormat.RGB -> GL11.GL_RGB
        TexFormat.RGBA -> GL11.GL_RGBA
        TexFormat.RG_F16 -> GL11.GL_RGBA16
        TexFormat.RGBA_F16 -> GL11.GL_RGBA16
    }

//fun Mat4f.setPerspective2(fovy: Float, aspect: Float, near: Float, far: Float): Mat4f {
//    val s = 1f / tan(fovy * (PI / 360.0)).toFloat()
//
//    matrix[offset + 0] = s / aspect
//    matrix[offset + 1] = 0.0f
//    matrix[offset + 2] = 0.0f
//    matrix[offset + 3] = 0.0f
//
//    matrix[offset + 4] = 0.0f
//    matrix[offset + 5] = s
//    matrix[offset + 6] = 0.0f
//    matrix[offset + 7] = 0.0f
//
//    matrix[offset + 8] = 0.0f
//    matrix[offset + 9] = 0.0f
//    matrix[offset + 10] = -far / (far - near)
//    matrix[offset + 11] = -1.0f
//
//    matrix[offset + 12] = 0.0f
//    matrix[offset + 13] = 0.0f
//    matrix[offset + 14] = -(far * near) / (far - near)
//    matrix[offset + 15] = 0.0f
//
//    return this
//}
//
//fun VkVertexInputBindingDescription.Buffer.setBindingDescription(vertices: IndexedVertexList, binding: Int = 0) {
//    binding(binding)
//    stride(vertices.strideBytesF)
//    inputRate(VK_VERTEX_INPUT_RATE_VERTEX)
//}
//
//fun VkVertexInputAttributeDescription.Buffer.setAttributeDescription(vertices: IndexedVertexList, binding: Int = 0) {
//    var iAttrib = 0
//    vertices.attributeOffsets.forEach { (attrib, off) ->
//        this[iAttrib].apply {
//            binding(binding)
//            location(iAttrib)
//            offset(off * 4)
//            when (attrib.type) {
//                AttributeType.FLOAT -> format(VK_FORMAT_R32_SFLOAT)
//                AttributeType.VEC_2F -> format(VK_FORMAT_R32G32_SFLOAT)
//                AttributeType.VEC_3F -> format(VK_FORMAT_R32G32B32_SFLOAT)
//                AttributeType.VEC_4F -> format(VK_FORMAT_R32G32B32A32_SFLOAT)
//                AttributeType.COLOR_4F -> format(VK_FORMAT_R32G32B32A32_SFLOAT)
//                else -> throw IllegalStateException("Attribute is not a float type")
//            }
//        }
//        iAttrib++
//    }
//}

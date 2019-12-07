package de.fabmax.kool.pipeline

import de.fabmax.kool.KoolContext
import de.fabmax.kool.pipeline.shading.CustomShader
import de.fabmax.kool.platform.vk.pipeline.ShaderStage
import de.fabmax.kool.scene.Mesh
import org.lwjgl.vulkan.VK10
import java.io.FileNotFoundException

actual class ShaderCode(val stages: List<ShaderStage>) {
    constructor(vararg stages: ShaderStage): this(stages.asList())

    actual val longHash: Long

    init {
        var hash = 0L
        stages.forEach { hash = (hash * 71023L) xor it.longHash }
        longHash = hash
    }

    companion object {
        fun codeFromResources(vertShaderName: String, fragShaderName: String): ShaderCode {
            val vertShaderCode = this::class.java.classLoader.getResourceAsStream(vertShaderName)?.use {
                ShaderStage.fromSource(vertShaderName, it, VK10.VK_SHADER_STAGE_VERTEX_BIT)
            } ?: throw FileNotFoundException("vertex shader resource not found: $vertShaderName")

            val fragShaderCode = this::class.java.classLoader.getResourceAsStream(fragShaderName)?.use {
                ShaderStage.fromSource(fragShaderName, it, VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
            } ?: throw FileNotFoundException("fragment shader resource not found: $vertShaderName")

            return ShaderCode(vertShaderCode, fragShaderCode)
        }

        fun shaderFromResources(vertShaderName: String, fragShaderName: String): (Mesh, Pipeline.BuildContext, KoolContext) -> CustomShader = { _, _, _ ->
            CustomShader(codeFromResources(vertShaderName, fragShaderName))
        }
    }
}
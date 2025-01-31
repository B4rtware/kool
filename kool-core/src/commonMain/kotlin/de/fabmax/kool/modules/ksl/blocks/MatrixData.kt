package de.fabmax.kool.modules.ksl.blocks

import de.fabmax.kool.KoolContext
import de.fabmax.kool.modules.ksl.KslShader
import de.fabmax.kool.modules.ksl.KslShaderListener
import de.fabmax.kool.modules.ksl.lang.KslDataBlock
import de.fabmax.kool.modules.ksl.lang.KslProgram
import de.fabmax.kool.pipeline.Pipeline
import de.fabmax.kool.pipeline.UniformMat4f
import de.fabmax.kool.pipeline.drawqueue.DrawCommand

fun KslProgram.mvpMatrix(): MvpMatrixData {
    return (dataBlocks.find { it is MvpMatrixData } as? MvpMatrixData) ?: MvpMatrixData(this)
}

fun KslProgram.modelMatrix(): ModelMatrixData {
    return (dataBlocks.find { it is ModelMatrixData } as? ModelMatrixData) ?: ModelMatrixData(this)
}

abstract class MatrixData(program: KslProgram, val uniformName: String) : KslDataBlock, KslShaderListener {
    val matrix = program.uniformMat4(uniformName)

    protected lateinit var uMatrix: UniformMat4f

    init {
        program.shaderListeners += this
        program.dataBlocks += this
    }

    override fun onShaderCreated(shader: KslShader, pipeline: Pipeline, ctx: KoolContext) {
        uMatrix = shader.uniforms[uniformName] as UniformMat4f
    }
}

class MvpMatrixData(program: KslProgram) : MatrixData(program, "uMvpMat") {
    override val name = NAME

    override fun onUpdate(cmd: DrawCommand) {
        uMatrix.value.set(cmd.mvpMat)
    }

    companion object {
        const val NAME = "MvpMatrixData"
    }
}

class ModelMatrixData(program: KslProgram) : MatrixData(program, "uModelMat") {
    override val name = NAME

    override fun onUpdate(cmd: DrawCommand) {
        uMatrix.value.set(cmd.modelMat)
    }

    companion object {
        const val NAME = "ModelMatrixData"
    }
}

package de.fabmax.kool.modules.ksl.blocks

import de.fabmax.kool.modules.ksl.lang.*
import de.fabmax.kool.scene.Light

class GetLightDirectionFromFragPos(parentScope: KslScopeBuilder) :
    KslFunction<KslTypeFloat3>(FUNC_NAME, KslTypeFloat3, parentScope.parentStage) {

    init {
        val fragPos = paramFloat3("fragPos")
        val encLightPos = paramFloat4("encLightPos")

        body {
            val dir = float3Var()
            `if` (encLightPos.w eq Light.Type.DIRECTIONAL.encoded.const) {
                dir set encLightPos.xyz * -(1f.const)
            }.`else` {
                dir set encLightPos.xyz - fragPos
            }
            return@body dir
        }
    }

    companion object {
        const val FUNC_NAME = "getLightDirectionFromFragPos"
    }
}

fun KslScopeBuilder.getLightDirectionFromFragPos(
    fragPos: KslExprFloat3,
    encodedLightPos: KslExprFloat4
): KslExprFloat3 {
    val func = parentStage.getOrCreateFunction(GetLightDirectionFromFragPos.FUNC_NAME) { GetLightDirectionFromFragPos(this) }
    return func(fragPos, encodedLightPos)
}

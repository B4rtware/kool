package de.fabmax.kool.util.aoMapGen

import de.fabmax.kool.KoolContext
import de.fabmax.kool.pipeline.NormalLinearDepthMapPass
import de.fabmax.kool.pipeline.Texture
import de.fabmax.kool.scene.PerspectiveCamera
import de.fabmax.kool.scene.Scene

class AmbientOcclusionHelper(scene: Scene) {

    val depthPass: NormalLinearDepthMapPass
    val aoPass: AmbientOcclusionPass
    val denoisePass: AoDenoisePass

    val aoMap: Texture
        get() = denoisePass.colorTexture

    init {
        val proxyCamera = ProxyCamera(scene.camera as PerspectiveCamera)
        depthPass = NormalLinearDepthMapPass(scene, 1600, 900)
        depthPass.camera = proxyCamera
        depthPass.isUpdateDrawNode = false
        depthPass.onBeforeCollectDrawCommands += { ctx ->
            proxyCamera.sync(scene.mainRenderPass.viewport, ctx)
        }

        aoPass = AmbientOcclusionPass(proxyCamera, depthPass)
        denoisePass = AoDenoisePass(aoPass)

        scene.addOffscreenPass(depthPass)
        scene.addOffscreenPass(aoPass)
        scene.addOffscreenPass(denoisePass)
    }

    fun setEnabled(enabled: Boolean) {
        depthPass.isEnabled = enabled
        aoPass.isEnabled = enabled
        denoisePass.isEnabled = enabled
    }

    private class ProxyCamera(val master: PerspectiveCamera) : PerspectiveCamera() {
        init {
            useViewportAspectRatio = false
            projCorrectionMode = ProjCorrectionMode.OFFSCREEN
        }

        fun sync(viewport: KoolContext.Viewport, ctx: KoolContext) {
            master.updateCamera(ctx, viewport)

            position.set(master.globalPos)
            lookAt.set(master.globalLookAt)
            up.set(master.globalUp)

            aspectRatio = master.aspectRatio
            fovY = master.fovY
            clipNear = master.clipNear
            clipFar = master.clipFar
        }
    }
}
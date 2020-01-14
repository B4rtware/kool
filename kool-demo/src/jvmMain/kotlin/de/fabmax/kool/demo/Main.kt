package de.fabmax.kool.demo

import de.fabmax.kool.AssetManager
import de.fabmax.kool.KoolContext
import de.fabmax.kool.createDefaultContext
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.randomF
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.pipelineConfig
import de.fabmax.kool.pipeline.shading.ModeledShader
import de.fabmax.kool.pipeline.shading.PbrShader
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.debugOverlay
import de.fabmax.kool.util.pbrMapGen.BrdfLutPass
import de.fabmax.kool.util.pbrMapGen.IrradianceMapPass

/**
 * @author fabmax
 */
fun main() {
//    Demo.setProperty("assetsBaseDir", "./docs/assets")
//    // launch demo
//    demo("modelDemo")

//    testScene()
    pbrDemo()
}

fun testScene() {
    val ctx = createDefaultContext()
    ctx.assetMgr.assetsBaseDir = "./docs/assets"

//    ctx.scenes += uiTestScene(ctx)
    ctx.scenes += bunnyScene(ctx)
//    ctx.scenes += brdfLutTestScene(ctx)
//    offscreenTest(ctx)
    ctx.scenes += debugOverlay(ctx)

    ctx.run()
}

fun offscreenTest(ctx: KoolContext) {
    val scene = scene {
        defaultCamTransform()

        +colorMesh {
            generator = {
                cube {
                    colorCube()
                    centerOrigin()
                }
            }
            pipelineConfig {
                shaderLoader = ModeledShader.vertexColor()
            }
        }
    }

    ctx.scenes += scene

//    ctx.assetMgr.loadAndPrepareTexture("skybox/hdri/newport_loft.rgbe.png") { tex ->
    ctx.assetMgr.loadAndPrepareTexture("skybox/hdri/vignaioli_night_2k.rgbe.png") { tex ->
//    ctx.assetMgr.loadAndPrepareTexture("skybox/hdri/spruit_sunrise_2k.rgbe.png") { tex ->
//    ctx.assetMgr.loadAndPrepareTexture("skybox/hdri/lakeside_2k.rgbe.png") { tex ->

        val cubeMapPass = IrradianceMapPass(tex)
        ctx.offscreenPasses += cubeMapPass.offscreenPass
        scene += Skybox(cubeMapPass.irradianceMap)

//        val cubeMapPass = hdriToCubeMapPass(tex)
//        ctx.offscreenPasses += cubeMapPass
//        scene += Skybox(cubeMapPass.textureCube)
    }
}


fun brdfLutTestScene(ctx: KoolContext): Scene = scene {
    camera = OrthographicCamera().apply {
        left = 0f
        right = 1f
        top = 1f
        bottom = 0f
        isKeepAspectRatio = false
    }

    val brdfLutPass = BrdfLutPass()
    ctx.offscreenPasses += brdfLutPass.offscreenPass

    +mesh(setOf(Attribute.POSITIONS, Attribute.TEXTURE_COORDS)) {
        generator = {
            rect {
                size.set(1f, 1f)
                fullTexCoords()
            }
        }
        pipelineConfig {
            shaderLoader = ModeledShader.textureColor()
            onPipelineCreated += {
                (it.shader as ModeledShader.TextureColor).textureSampler.texture = brdfLutPass.brdfLut
            }
        }
    }
}

fun bunnyScene(ctx: KoolContext): Scene = scene {
    defaultCamTransform()

    ctx.clearColor = Color.fromHsv(0f, 0f, 0.15f, 1f)

    val cyanLight = Light().setSpot(Vec3f(10f, 7f, -10f), Vec3f.X_AXIS, 45f).setColor(Color.MD_CYAN.gamma(), 500f)
    val redLight = Light().setSpot(Vec3f(-10f, 7f, 10f), Vec3f.X_AXIS, 45f).setColor(Color.MD_RED.gamma(), 500f)

//    val cyanLight = Light().setPoint(Vec3f(10f, 7f, -10f)).setColor(Color.MD_CYAN.gamma(), 150f)
//    val redLight = Light().setPoint(Vec3f(-10f, 7f, 10f)).setColor(Color.MD_RED.gamma(), 150f)
//    val yellowLight = Light().setPoint(Vec3f(-10f, 7f, -10f)).setColor(Color.MD_AMBER.gamma(), 150f)
//    val greenLight = Light().setPoint(Vec3f(10f, 7f, 10f)).setColor(Color.MD_GREEN.gamma(), 150f)

    lighting.lights.clear()
    lighting.lights.add(cyanLight)
    lighting.lights.add(redLight)
//    lighting.lights.add(yellowLight)
//    lighting.lights.add(greenLight)

    +LightMesh(cyanLight)
    +LightMesh(redLight)
//    +LightMesh(yellowLight)
//    +LightMesh(greenLight)

    +Skybox("skybox/y-up/sky_ft.jpg", "skybox/y-up/sky_bk.jpg",
            "skybox/y-up/sky_lt.jpg", "skybox/y-up/sky_rt.jpg",
            "skybox/y-up/sky_up.jpg", "skybox/y-up/sky_dn.jpg")

    +transformGroup {
        translate(0f, 1.5f, 0f)

        +colorMesh {
            generator = {
                color = Color.GRAY
                withTransform {
                    translate(0f, -3.75f, 0f)
                    rotate(90f, Vec3f.NEG_X_AXIS)
                    rect {
                        size.set(50f, 50f)
                        origin.set(-size.x / 2, -size.y/2, 0f)
                    }
                }
            }
            val shader = PbrShader()
            shader.roughness = 0.5f
            pipelineConfig { shaderLoader = shader::setup }
        }

        loadModel(ctx.assetMgr) { model ->
            println("adding mesh: ${model.meshData.numVertices} verts, ${model.meshData.numIndices / 3} tris")

            val colorMesh = mesh(setOf(Attribute.POSITIONS, Attribute.COLORS, Attribute.NORMALS)) {
                val shader = PbrShader()
                shader.roughness = 0.15f
                pipelineConfig { shaderLoader = shader::setup }

                val target = meshData
                target.vertexList.addFrom(model.meshData.vertexList)
                target.vertexList.forEach { it.color.set(Color.WHITE) }
            }
            +colorMesh
            println("done")
        }

        onPreRender += { ctx ->
            rotate(10f * ctx.deltaT, 0f, 1f, 0f)
        }
    }
}

class LightMesh(val light: Light) : TransformGroup() {
    private val lightPos = Vec3f(light.position)
    private val rotOff = randomF(0f, 3f)
    init {
        val lightMesh = colorMesh {
            generator = {
                color = light.color.withAlpha(1f)
                sphere {
                    radius = 0.2f
                }
            }
            pipelineConfig { shaderLoader = ModeledShader.vertexColor() }
        }
        +lightMesh

        onPreRender += { ctx ->
            setIdentity()
            rotate(ctx.time.toFloat() * -20f, Vec3f.Y_AXIS)
            translate(lightPos)
            light.position.set(lightMesh.globalCenter)
            light.direction.set(lightMesh.globalCenter).scale(-1f).norm()

//            val r = cos(ctx.time / 15 + rotOff).toFloat()
//            light.direction.rotate(r * 10f, Vec3f.X_AXIS)
//            light.direction.rotate(r * 10f, Vec3f.Z_AXIS)
        }
    }
}

fun loadModel(assetMgr: AssetManager, recv: (Mesh) -> Unit) {
    assetMgr.loadModel("bunny.kmfz") { model ->
        val scale = 0.05f
        if (model != null) {
            val mesh = model.meshes[0].toMesh()
            mesh.meshData.vertexList.forEach {
                it.position.scale(scale)
            }
            recv(mesh)
        }
    }
}
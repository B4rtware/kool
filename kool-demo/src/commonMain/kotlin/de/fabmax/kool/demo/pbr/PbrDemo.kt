package de.fabmax.kool.demo.pbr

import de.fabmax.kool.Assets
import de.fabmax.kool.KoolContext
import de.fabmax.kool.demo.*
import de.fabmax.kool.demo.menu.DemoMenu
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.FilterMethod
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.pipeline.TextureProps
import de.fabmax.kool.pipeline.ibl.EnvironmentHelper
import de.fabmax.kool.pipeline.ibl.EnvironmentMaps
import de.fabmax.kool.scene.*
import de.fabmax.kool.scene.geometry.IndexedVertexList
import de.fabmax.kool.scene.geometry.MeshBuilder
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.RenderLoop
import de.fabmax.kool.util.Time
import de.fabmax.kool.util.launchDelayed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author fabmax
 */

class PbrDemo : DemoScene("PBR Materials") {

    private lateinit var skybox: Skybox.Cube
    private lateinit var envMaps: EnvironmentMaps

    private val loadedHdris = Array<Texture2d?>(hdriTextures.size) { null }

    private val sphereProto = SphereProto()
    private val pbrContent = listOf(
        PbrMaterialContent(sphereProto),
        ColorGridContent(sphereProto),
        RoughnesMetalGridContent(sphereProto)
    )
    private val selectedContentIdx = mutableStateOf(0)
    private val selectedHdriIdx = mutableStateOf(0)
    private val selectedLightIdx = mutableStateOf(0)

    private val selectedContent: PbrContent get() = pbrContent[selectedContentIdx.value]
    private val selectedLightSetup: LightSetup get() = lightSetups[selectedLightIdx.value]

    private val isIbl = mutableStateOf(true).onChange {
        pbrContent.forEach { c -> c.setUseImageBasedLighting(it) }
    }
    private val isAutoRotate = mutableStateOf(true).onChange {
        pbrContent.forEach { c -> c.autoRotate = it }
    }

    override fun lateInit(ctx: KoolContext) {
        mainScene.onDispose += {
            loadedHdris.forEach { it?.dispose() }
            envMaps.dispose()
        }
    }

    override fun Scene.setupMainScene(ctx: KoolContext) {
        selectedLightSetup.setup(this)

        orbitCamera {
            // let the camera slowly rotate around vertical axis
            onUpdate += {
                if (isAutoRotate.value) {
                    verticalRotation += Time.deltaT * 2f
                }
            }
            zoomMethod = OrbitInputTransform.ZoomMethod.ZOOM_CENTER
            setZoom(20.0)
        }

        loadHdri(selectedHdriIdx.value) { hdri ->
            envMaps = EnvironmentHelper.hdriEnvironment(this, hdri, false)
            skybox = Skybox.cube(envMaps.reflectionMap, 1f)
            this += skybox

            pbrContent.forEach {
                addNode(it.createContent(this, envMaps, ctx))
            }
            selectedContent.show()
        }
    }

    override fun createMenu(menu: DemoMenu, ctx: KoolContext) = menuSurface {
        val comboW = UiSizes.baseSize * 3.5f
        MenuRow {
            Text("Scene") { labelStyle(Grow.Std) }
            ComboBox {
                modifier
                    .width(comboW)
                    .items(pbrContent)
                    .selectedIndex(selectedContentIdx.use())
                    .onItemSelected {
                        selectedContent.hide()
                        selectedContentIdx.set(it)
                        selectedContent.show()
                    }
            }
        }
        with(selectedContent) { createContentMenu() }

        Text("Settings") { sectionTitleStyle() }
        LabeledSwitch("Image based lighting", isIbl)
        MenuRow {
            Text("Environment") { labelStyle(Grow.Std) }
            ComboBox {
                modifier
                    .width(comboW)
                    .items(hdriTextures)
                    .selectedIndex(selectedHdriIdx.use())
                    .onItemSelected {
                        selectedHdriIdx.set(it)
                        updateHdri(it)
                    }
            }
        }
        MenuRow {
            Text("Discrete lights") { labelStyle(Grow.Std) }
            ComboBox {
                modifier
                    .width(comboW)
                    .items(lightSetups)
                    .selectedIndex(selectedLightIdx.use())
                    .onItemSelected {
                        selectedLightIdx.set(it)
                        selectedLightSetup.setup(mainScene)
                    }
            }
        }
        LabeledSwitch("Auto rotate view", isAutoRotate)
    }

    private fun updateHdri(idx: Int) {
        loadHdri(idx) { tex ->
            envMaps.let { oldEnvMap -> launchDelayed(1) { oldEnvMap.dispose() } }
            envMaps = EnvironmentHelper.hdriEnvironment(mainScene, tex, false)
            skybox.skyboxShader.setSingleSky(envMaps.reflectionMap)
            pbrContent.forEach { it.updateEnvironmentMap(envMaps) }
        }
    }

    private fun loadHdri(idx: Int, recv: (Texture2d) -> Unit) {
        val tex = loadedHdris[idx]
        if (tex == null) {
            Assets.launch {
                val loadedTex = loadTexture2d(hdriTextures[idx].hdriPath, hdriTexProps)
                withContext(Dispatchers.RenderLoop) {
                    loadedHdris[idx] = loadedTex
                    recv(loadedTex)
                }
            }
        } else {
            recv(tex)
        }
    }

    private class Hdri(val hdriPath: String, val name: String) {
        override fun toString() = name
    }

    private class LightSetup(val name: String, val setup: Scene.() -> Unit) {
        override fun toString() = name
    }

    abstract class PbrContent(val name: String) {
        var content: Node? = null
        var autoRotate = true

        fun show() {
            content?.isVisible = true
        }

        fun hide() {
            content?.isVisible = false
        }

        override fun toString() = name

        abstract fun UiScope.createContentMenu()
        abstract fun setUseImageBasedLighting(enabled: Boolean)
        abstract fun createContent(scene: Scene, envMaps: EnvironmentMaps, ctx: KoolContext): Node
        abstract fun updateEnvironmentMap(envMaps: EnvironmentMaps)
    }

    class SphereProto {
        val detailSphere = IndexedVertexList(Attribute.POSITIONS, Attribute.TEXTURE_COORDS, Attribute.NORMALS, Attribute.TANGENTS)
        val simpleSphere = IndexedVertexList(Attribute.POSITIONS, Attribute.NORMALS)

        init {
            MeshBuilder(detailSphere).apply {
                vertexModFun = {
                    texCoord.x *= 4
                    texCoord.y *= 2
                }
                uvSphere {
                    steps = 700
                    radius = 7f
                }
            }
            detailSphere.generateTangents()

            MeshBuilder(simpleSphere).apply {
                uvSphere {
                    steps = 100
                    radius = 1f
                }
            }
        }
    }

    companion object {
        // HDRIs are encoded as RGBE images, use nearest sampling to not mess up the exponent
        private val hdriTexProps = TextureProps(
                minFilter = FilterMethod.NEAREST,
                magFilter = FilterMethod.NEAREST,
                mipMapping = false,
                maxAnisotropy = 1)

        private val hdriTextures = listOf(
                Hdri("${DemoLoader.hdriPath}/syferfontein_0d_clear_1k.rgbe.png", "South Africa"),
                Hdri("${DemoLoader.hdriPath}/circus_arena_1k.rgbe.png", "Circus"),
                Hdri("${DemoLoader.hdriPath}/newport_loft.rgbe.png", "Loft"),
                Hdri("${DemoLoader.hdriPath}/shanghai_bund_1k.rgbe.png", "Shanghai"),
                Hdri("${DemoLoader.hdriPath}/mossy_forest_1k.rgbe.png", "Mossy forest")
        )

        private const val lightStrength = 250f
        private const val lightExtent = 10f
        private val lightSetups = listOf(
                LightSetup("None") { lighting.lights.clear() },

                LightSetup("Front x1") {
                    val light1 = Light().setPoint(Vec3f(0f, 0f, lightExtent * 1.5f)).setColor(Color.WHITE, lightStrength * 2f)
                    lighting.lights.clear()
                    lighting.lights.add(light1)
                },

                LightSetup("Front x4") {
                    val light1 = Light().setPoint(Vec3f(lightExtent, lightExtent, lightExtent)).setColor(Color.WHITE, lightStrength)
                    val light2 = Light().setPoint(Vec3f(-lightExtent, -lightExtent, lightExtent)).setColor(Color.WHITE, lightStrength)
                    val light3 = Light().setPoint(Vec3f(-lightExtent, lightExtent, lightExtent)).setColor(Color.WHITE, lightStrength)
                    val light4 = Light().setPoint(Vec3f(lightExtent, -lightExtent, lightExtent)).setColor(Color.WHITE, lightStrength)
                    lighting.lights.clear()
                    lighting.lights.add(light1)
                    lighting.lights.add(light2)
                    lighting.lights.add(light3)
                    lighting.lights.add(light4)
                },

                LightSetup("Top x1") {
                    val light1 = Light().setPoint(Vec3f(0f, lightExtent * 1.5f, 0f)).setColor(Color.WHITE, lightStrength * 2f)
                    lighting.lights.clear()
                    lighting.lights.add(light1)
                },

                LightSetup("Top x4") {
                    val light1 = Light().setPoint(Vec3f(lightExtent, lightExtent, lightExtent)).setColor(Color.WHITE, lightStrength)
                    val light2 = Light().setPoint(Vec3f(-lightExtent, lightExtent, -lightExtent)).setColor(Color.WHITE, lightStrength)
                    val light3 = Light().setPoint(Vec3f(-lightExtent, lightExtent, lightExtent)).setColor(Color.WHITE, lightStrength)
                    val light4 = Light().setPoint(Vec3f(lightExtent, lightExtent, -lightExtent)).setColor(Color.WHITE, lightStrength)
                    lighting.lights.clear()
                    lighting.lights.add(light1)
                    lighting.lights.add(light2)
                    lighting.lights.add(light3)
                    lighting.lights.add(light4)
                }
        )
    }
}
package de.fabmax.kool.demo

import de.fabmax.kool.platform.RenderContext
import de.fabmax.kool.scene.*
import de.fabmax.kool.scene.ui.*
import de.fabmax.kool.shading.ColorModel
import de.fabmax.kool.shading.LightModel
import de.fabmax.kool.shading.basicShader
import de.fabmax.kool.util.*

/**
 * @author fabmax
 */

fun uiDemo(ctx: RenderContext) {

//    ctx.scene.camera = OrthographicCamera().apply {
//        clipToViewport = true
//    }

    ctx.clearColor = color("00323F")
    ctx.scene.camera = PerspectiveCamera().apply {
        position.set(0f, 0f, 15f)
    }

    // Create scene contents
    ctx.scene.root = group {
        +sphericalInputTransform { +ctx.scene.camera }

        +transformGroup {
            onRender += { ctx ->
                setIdentity()
                translate(0f, 0f, -7f)
                rotate((ctx.time * 60).toFloat(), Vec3f.X_AXIS)
                rotate((ctx.time * 17).toFloat(), Vec3f.Y_AXIS)
            }
            +colorMesh {
                generator = {
                    scale(5f, 5f, 5f)
                    cube {
                        centerOrigin()
                        colorCube()
                    }
                }
            }
        }

        +UiRoot(300f).apply {
            theme = UiTheme.DARK

            translate(-globalWidth /2, -globalHeight/2, 0f)
            scaleContentTo(dps(400f))

            +ToggleButton("toggle-button").apply {
                layoutSpec.setOrigin(dps(50f), pcs(-15f), uns(0f))
                layoutSpec.setSize(dps(300f), pcs(15f), uns(0f))

                text = "Toggle Button"
            }

            +Slider("slider", 0.4f, 1f, 1f).apply {
                layoutSpec.setOrigin(dps(50f), pcs(-35f), uns(0f))
                layoutSpec.setSize(dps(300f), pcs(15f), uns(0f))

                onValueChanged += { value ->
                    root?.alpha = value
                }
            }

            +Button("darkTheme").apply {
                layoutSpec.setOrigin(dps(50f), pcs(-55f), uns(0f))
                layoutSpec.setSize(dps(300f), pcs(15f), uns(0f))
                text = "Dark Theme"

                onClick += { _,_,_ ->
                    theme = UiTheme.DARK
                }
            }

            +Button("lightTheme").apply {
                layoutSpec.setOrigin(dps(50f), pcs(-75f), uns(0f))
                layoutSpec.setSize(dps(300f), pcs(15f), uns(0f))
                text = "Light Theme"

                onClick += { _,_,_ ->
                    theme = UiTheme.LIGHT
                }
            }
        }
    }
    ctx.run()
}

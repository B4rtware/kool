package de.fabmax.kool.demo

import de.fabmax.kool.Input
import de.fabmax.kool.KoolContext
import de.fabmax.kool.demo.menu.DemoMenu
import de.fabmax.kool.math.*
import de.fabmax.kool.modules.ksl.KslBlinnPhongShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.scene.colorMesh
import de.fabmax.kool.scene.defaultOrbitCamera
import de.fabmax.kool.scene.group
import de.fabmax.kool.toString
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.Gizmo
import de.fabmax.kool.util.InputStack
import de.fabmax.kool.util.MdColor

class GizmoTest : DemoScene("Gizmo Test") {
    private val gizmo1 = Gizmo()
    private val gizmo2 = Gizmo()

    private val transform1 = TransformProps(gizmo1)
    private val transform2 = TransformProps(gizmo2)

    override fun Scene.setupMainScene(ctx: KoolContext) {
        gizmo1.gizmoListener = object : Gizmo.GizmoListener {
            override fun onDragAxis(axis: Vec3f, distance: Float, targetTransform: Mat4d, ctx: KoolContext) {
                targetTransform.translate(axis.x * distance, axis.y * distance, axis.z * distance)
            }

            override fun onDragPlane(planeNormal: Vec3f, dragPosition: Vec3f, targetTransform: Mat4d, ctx: KoolContext) {
                targetTransform.translate(dragPosition)
            }

            override fun onDragRotate(rotationAxis: Vec3f, angle: Float, targetTransform: Mat4d, ctx: KoolContext) {
                targetTransform.rotate(angle, rotationAxis)
            }
        }

        group {
            onUpdate += {
                gizmo1.getGizmoTransform(this)
            }

            colorMesh {
                generate {
                    cube {
                        centered()
                    }
                }
                shader = KslBlinnPhongShader {
                    color {
                        vertexColor()
                    }
                }
            }

        }

        // gizmo must be added after scene objects for correct depth / alpha behavior
        addNode(gizmo1)

        gizmo2.setFixedScale(1f)
        gizmo2.axisHandleX.set(4f, 0f, 0f)
        gizmo2.setGizmoTransform(Mat4d().translate(0.0, 0.0, 3.0))
        gizmo2.properties = Gizmo.GizmoProperties(
            axisColorX = Color.WHITE,
            axisColorNegX = Color.WHITE,
            axisHandleColorX = MdColor.AMBER,
            axisHandleColorNegX = MdColor.AMBER,
            isOnlyShowAxisTowardsCam = false,
            rotationHandleRadius = 0.75f,
            hasAxisY = false,
            hasAxisNegY = false,
            hasRotationX = false,
            hasRotationY = true,
            hasRotationZ = false,
            hasPlaneXY = false,
            hasPlaneXZ = true,
            hasPlaneYZ = false
        )
        addNode(gizmo2)

        var axX = 1f
        var axNegX = -1f
        gizmo2.gizmoListener = object : Gizmo.GizmoListener {
            override fun onDragStart(ctx: KoolContext) {
                axX = gizmo2.axisHandleX.x
                axNegX = gizmo2.axisHandleNegX.x
            }

            override fun onDragAxis(axis: Vec3f, distance: Float, targetTransform: Mat4d, ctx: KoolContext) {
                if (axis.z != 0f || Input.isAltDown) {
                    targetTransform.translate(axis.x * distance, axis.y * distance, axis.z * distance)
                } else if (axis.x > 0f) {
                    gizmo2.axisHandleX.x = max(0.1f, axX + distance)
                } else {
                    gizmo2.axisHandleNegX.x = min(-0.1f, axNegX - distance)
                }
                gizmo2.updateMesh()
            }

            override fun onDragPlane(planeNormal: Vec3f, dragPosition: Vec3f, targetTransform: Mat4d, ctx: KoolContext) {
                targetTransform.translate(dragPosition)
            }

            override fun onDragRotate(rotationAxis: Vec3f, angle: Float, targetTransform: Mat4d, ctx: KoolContext) {
                targetTransform.rotate(angle, rotationAxis)
            }
        }

        InputStack.defaultInputHandler.pointerListeners += gizmo1
        InputStack.defaultInputHandler.pointerListeners += gizmo2

        // add cam transform after gizmo, so that gizmo can consume drag events before cam transform
        defaultOrbitCamera()

        onUpdate += {
            transform1.update()
            transform2.update()
        }

        onDispose += {
            InputStack.defaultInputHandler.pointerListeners -= gizmo1
            InputStack.defaultInputHandler.pointerListeners -= gizmo2
        }
        camera.setClipRange(0.2f, 500f)
    }

    override fun createMenu(menu: DemoMenu, ctx: KoolContext) = menuSurface {
        Text("Gizmo 1") { sectionTitleStyle() }
        val isDynScale1 = remember(gizmo1.isDynamicScale()).onChange {
            if (it) gizmo1.setDynamicScale() else gizmo1.setFixedScale()
        }
        LabeledSwitch("Distance independent scale", isDynScale1)
        translation(transform1)
        rotation(transform1)

        Text("Gizmo 2") { sectionTitleStyle() }
        val isDynScale2 = remember(gizmo2.isDynamicScale()).onChange {
            if (it) gizmo2.setDynamicScale() else gizmo2.setFixedScale()
        }
        LabeledSwitch("Dynamic scale", isDynScale2)
        translation(transform2)
        rotation(transform2)
    }

    private fun UiScope.translation(props: TransformProps) = MenuRow {
        Column(sizes.largeGap * 4f) {
            Text("Location") {
                modifier.margin(vertical = sizes.smallGap * 0.75f)
            }
        }
        Column(Grow.Std) {
            Row(Grow.Std) {
                Text("x:") { modifier.alignY(AlignmentY.Center) }
                TransformTextField(props.tx, 3)
            }
            Row(Grow.Std) {
                Text("y:") { modifier.alignY(AlignmentY.Center) }
                TransformTextField(props.ty, 3)
            }
            Row(Grow.Std) {
                Text("z:") { modifier.alignY(AlignmentY.Center) }
                TransformTextField(props.tz, 3)
            }
        }
    }

    private fun UiScope.rotation(props: TransformProps) = MenuRow {
        Column(sizes.largeGap * 4f) {
            Text("Rotation") {
                modifier.margin(vertical = sizes.smallGap * 0.75f)
            }
        }
        Column(Grow.Std) {
            Row(Grow.Std) {
                Text("x:") { modifier.alignY(AlignmentY.Center) }
                TransformTextField(props.rx, 1)
            }
            Row(Grow.Std) {
                Text("y:") { modifier.alignY(AlignmentY.Center) }
                TransformTextField(props.ry, 1)
            }
            Row(Grow.Std) {
                Text("z:") { modifier.alignY(AlignmentY.Center) }
                TransformTextField(props.rz, 1)
            }
        }
    }

    private fun UiScope.TransformTextField(state: MutableStateValue<Float>, precision: Int) = TextField {
        var text by remember(state.value.toString(precision))
        if (!isFocused.value) {
            text = state.use().toString(precision)
        }
        modifier
            .text(text)
            .padding(vertical = sizes.smallGap * 0.75f)
            .margin(start = sizes.gap)
            .width(Grow.Std)
            .textAlignX(AlignmentX.End)
            .onEnterPressed { text = state.use().toString(precision) }
            .onChange { txt ->
                text = txt
                txt.toFloatOrNull()?.let {
                    state.set(it)
                }
            }
    }

    class TransformProps(val gizmo: Gizmo) {
        private var isUpdateFromGizmo = false

        val tx = mutableStateOf(0f).onChange { updateTranslation(x = it) }
        val ty = mutableStateOf(0f).onChange { updateTranslation(y = it) }
        val tz = mutableStateOf(0f).onChange { updateTranslation(z = it) }

        val rx = mutableStateOf(0f).onChange { updateRotation(x = it) }
        val ry = mutableStateOf(0f).onChange { updateRotation(y = it) }
        val rz = mutableStateOf(0f).onChange { updateRotation(z = it) }

        private val tmpMat4 = Mat4d()
        private val tmpMat3 = Mat3f()
        private val tmpVec = MutableVec3f()

        fun update() {
            gizmo.getGizmoTransform(tmpMat4)

            isUpdateFromGizmo = true
            tmpMat4.transform(tmpVec.set(Vec3f.ZERO))
            tx.set(tmpVec.x)
            ty.set(tmpVec.y)
            tz.set(tmpVec.z)

            tmpMat4.getRotation(tmpMat3).getEulerAngles(tmpVec)
            rx.set(tmpVec.x)
            ry.set(tmpVec.y)
            rz.set(tmpVec.z)
            isUpdateFromGizmo = false
        }

        private fun updateTranslation(x: Float = tx.value, y: Float = ty.value, z: Float = tz.value) {
            if (isUpdateFromGizmo) {
                return
            }
            gizmo.setTranslation(tmpVec.set(x, y, z))
        }

        private fun updateRotation(x: Float = rx.value, y: Float = ry.value, z: Float = rz.value) {
            if (isUpdateFromGizmo) {
                return
            }
            gizmo.setEulerAngles(tmpVec.set(x, y, z))
        }
    }
}
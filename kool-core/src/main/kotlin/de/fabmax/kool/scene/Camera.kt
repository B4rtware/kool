package de.fabmax.kool.scene

import de.fabmax.kool.InputManager
import de.fabmax.kool.RenderContext
import de.fabmax.kool.math.isZero
import de.fabmax.kool.math.toDeg
import de.fabmax.kool.math.toRad
import de.fabmax.kool.util.*
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.tan

/**
 * @author fabmax
 */
abstract class Camera(name: String = "camera") : Node(name) {

    val position = MutableVec3f(0f, 0f, 1f)
    val lookAt = MutableVec3f(Vec3f.Companion.ZERO)
    val up = MutableVec3f(Vec3f.Companion.Y_AXIS)

    var aspectRatio = 1.0f
        protected set

    val globalPos: Vec3f get() = globalPosMut
    val globalLookAt: Vec3f get() = globalLookAtMut
    val globalUp: Vec3f get() = globalUpMut
    val globalRight: Vec3f get() = globalRightMut
    val globalLookDir: Vec3f get() = globalLookDirMut
    var globalRange = 0f
        protected set

    protected val globalPosMut = MutableVec3f()
    protected val globalLookAtMut = MutableVec3f()
    protected val globalUpMut = MutableVec3f()
    protected val globalRightMut = MutableVec3f()
    protected val globalLookDirMut = MutableVec3f()

    protected val view = Mat4f()
    protected val invView = Mat4f()
    protected val mvp = Mat4f()
    protected val invMvp = Mat4f()

    // we need a bunch of temporary vectors, keep them as members (#perfmatters)
    private val tmpVec3 = MutableVec3f()
    private val tmpVec4 = MutableVec4f()

    fun updateCamera(ctx: RenderContext) {
        aspectRatio = ctx.viewport.aspectRatio

        updateViewMatrix(ctx)
        updateProjectionMatrix(ctx)

        ctx.mvpState.update(ctx)
        mvp.set(ctx.mvpState.mvpMatrix)
        mvp.invert(invMvp)
    }

    protected open fun updateViewMatrix(ctx: RenderContext) {
        toGlobalCoords(globalPosMut.set(position))
        toGlobalCoords(globalLookAtMut.set(lookAt))
        toGlobalCoords(globalUpMut.set(up), 0f).norm()

        globalLookDirMut.set(globalLookAtMut).subtract(globalPosMut)
        globalRange = globalLookDirMut.length()
        //globalLookDirMut.scale(1f / globalRange)
        globalLookDirMut.norm()

        globalLookDirMut.cross(globalUpMut, globalRightMut).norm()
        globalRightMut.cross(globalLookDirMut, globalUpMut).norm()

        view.setLookAt(globalPosMut, globalLookAtMut, globalUpMut)
        view.invert(invView)
        ctx.mvpState.viewMatrix.set(view)
    }

    abstract protected fun updateProjectionMatrix(ctx: RenderContext)

    fun computePickRay(pickRay: Ray, ptr: InputManager.Pointer, ctx: RenderContext): Boolean {
        return ptr.isValid && computePickRay(pickRay, ptr.x, ptr.y, ctx)
    }

    fun computePickRay(pickRay: Ray, screenX: Float, screenY: Float, ctx: RenderContext): Boolean {
        var valid = unProjectScreen(tmpVec3.set(screenX, screenY, 0f), ctx, pickRay.origin)
        valid = valid && unProjectScreen(tmpVec3.set(screenX, screenY, 1f), ctx, pickRay.direction)

        if (valid) {
            pickRay.direction.subtract(pickRay.origin)
            pickRay.direction.norm()
        }

        return valid
    }

    fun initRayTes(rayTest: RayTest, ptr: InputManager.Pointer, ctx: RenderContext): Boolean {
        return ptr.isValid && initRayTes(rayTest, ptr.x, ptr.y, ctx)
    }

    fun initRayTes(rayTest: RayTest, screenX: Float, screenY: Float, ctx: RenderContext): Boolean {
        rayTest.clear()
        return computePickRay(rayTest.ray, screenX, screenY, ctx)
    }

    abstract fun computeFrustumPlane(z: Float, result: FrustumPlane)

    abstract fun isInFrustum(node: Node): Boolean

    fun project(world: Vec3f, result: MutableVec3f): Boolean {
        tmpVec4.set(world.x, world.y, world.z, 1f)
        mvp.transform(tmpVec4)
        if (tmpVec4.w.isZero()) {
            return false
        }
        result.set(tmpVec4.x, tmpVec4.y, tmpVec4.z).scale(1f / tmpVec4.w)
        return true
    }

    fun project(world: Vec3f, result: MutableVec4f): MutableVec4f =
            mvp.transform(result.set(world.x, world.y, world.z, 1f))

    fun projectScreen(world: Vec3f, ctx: RenderContext, result: MutableVec3f): Boolean {
        if (!project(world, result)) {
            return false
        }
        result.x = (1 + result.x) * 0.5f * ctx.viewport.width + ctx.viewport.x
        result.y = ctx.windowHeight - ((1 + result.y) * 0.5f * ctx.viewport.height + ctx.viewport.y)
        result.z = (1 + result.z) * 0.5f
        return true
    }

    fun unProjectScreen(screen: Vec3f, ctx: RenderContext, result: MutableVec3f): Boolean {
        val x = screen.x - ctx.viewport.x
        val y = (ctx.windowHeight - screen.y) - ctx.viewport.y
        tmpVec4.set(2f * x / ctx.viewport.width - 1f, 2f * y / ctx.viewport.height - 1f, 2f * screen.z - 1f, 1f)
        invMvp.transform(tmpVec4)
        val s = 1f / tmpVec4.w
        result.set(tmpVec4.x * s, tmpVec4.y * s, tmpVec4.z * s)
        return true
    }
}

class OrthographicCamera(name: String = "orthographicCam") : Camera(name) {
    var left = -10.0f
    var right = 10.0f
    var bottom = -10.0f
    var top = 10.0f
    var near = -10.0f
    var far = 10.0f

    var clipToViewport = false
    var keepAspectRatio = true

    private val tmpNodeCenter = MutableVec3f()
    private val tmpNodeExtent = MutableVec3f()

    fun setCentered(height: Float, near: Float, far: Float) {
        top = height * 0.5f
        bottom = -top
        right = aspectRatio * top
        left = -right
        this.near = near
        this.far = far
    }

    override fun updateViewMatrix(ctx: RenderContext) {
        super.updateViewMatrix(ctx)
        globalLookDir
    }

    override fun updateProjectionMatrix(ctx: RenderContext) {
        if (clipToViewport) {
            left = 0f
            right = ctx.viewport.width.toFloat()
            bottom = 0f
            top = ctx.viewport.height.toFloat()

        } else if (keepAspectRatio) {
            val h = top - bottom
            val w = aspectRatio * h
            val xCenter = left + (right - left) * 0.5f
            left = xCenter - w * 0.5f
            right = xCenter + w * 0.5f
        }
        if (left != right && bottom != top && near != far) {
            ctx.mvpState.projMatrix.setOrthographic(left, right, bottom, top, near, far)
        }
    }

    override fun computeFrustumPlane(z: Float, result: FrustumPlane) {
        val zd = near + (far - near) * z
        invView.transform(result.upperLeft.set(left, top, -zd))
        invView.transform(result.upperRight.set(right, top, -zd))
        invView.transform(result.lowerLeft.set(left, bottom, -zd))
        invView.transform(result.lowerRight.set(right, bottom, -zd))
    }

    /**
     * Tests whether a node intersects this camera's view frustum.
     * Similar approach as for perspective camera, but even simpler because no perspective is involved.
     */
    override fun isInFrustum(node: Node): Boolean {
        val nodeRadius = node.globalRadius
        tmpNodeCenter.set(node.globalCenter)
        tmpNodeCenter.subtract(globalPos)

        val x = tmpNodeCenter.dot(globalRight)
        if (x > right + nodeRadius || x < left - nodeRadius) {
            // node's bounding sphere is either left or right of frustum
            return false
        }

        val y = tmpNodeCenter.dot(globalUp)
        if (y > top + nodeRadius || y < bottom - nodeRadius) {
            // node's bounding sphere is either above or below frustum
            return false
        }

        val z = tmpNodeCenter.dot(globalLookDir)
        if (z > far + nodeRadius || z < near - nodeRadius) {
            // node's bounding sphere is either in front of near or behind far plane
            return false
        }
        return true
    }
}

class PerspectiveCamera(name: String = "perspectiveCam") : Camera(name) {
    var clipNear = 0.2f
    var clipFar = 200.0f

    var fovy = 60.0f
    var fovX = 0f
        private set

    private var sphereFacX = 1f
    private var speherFacY = 1f
    private var tangX = 1f
    private var tangY = 1f

    private val tmpNodeCenter = MutableVec3f()
    private val tmpNodeExtent = MutableVec3f()

    override fun updateProjectionMatrix(ctx: RenderContext) {
        ctx.mvpState.projMatrix.setPerspective(fovy, aspectRatio, clipNear, clipFar)

        // compute intermediate values needed for view frustum culling
        val angY = fovy.toRad() / 2f
        speherFacY = 1f / cos(angY)
        tangY = tan(angY)

        val angX = atan(tangY * aspectRatio)
        sphereFacX = 1f / cos(angX)
        tangX = tan(angX)
        fovX = (angX * 2).toDeg()
    }

    override fun computeFrustumPlane(z: Float, result: FrustumPlane) {
        val zd = clipNear + (clipFar - clipNear) * z
        val x = zd * tangX
        val y = zd * tangY

        invView.transform(result.upperLeft.set(-x, y, -zd))
        invView.transform(result.upperRight.set(x, y, -zd))
        invView.transform(result.lowerLeft.set(-x, -y, -zd))
        invView.transform(result.lowerRight.set(x, -y, -zd))
    }

    /**
     * Tests whether a node intersects this camera's view frustum. For performance reasons the node's bounding sphere
     * is used instead of the bounding box.
     * Implements the radar approach from http://www.lighthouse3d.com/tutorials/view-frustum-culling/
     */
    override fun isInFrustum(node: Node): Boolean {
        val nodeRadius = node.globalRadius
        tmpNodeCenter.set(node.globalCenter)
        tmpNodeCenter.subtract(globalPos)

        var z = tmpNodeCenter.dot(globalLookDir)
        if (z > clipFar + nodeRadius || z < clipNear - nodeRadius) {
            // node's bounding sphere is either in front of near or behind far plane
            return false
        }

        val y = tmpNodeCenter.dot(globalUp)
        var d = nodeRadius * speherFacY
        z *= tangY
        if (y > z + d || y < -z - d) {
            // node's bounding sphere is either above or below view frustum
            return false
        }

        val x = tmpNodeCenter.dot(globalRight)
        d = nodeRadius * sphereFacX
        z *= aspectRatio
        if (x > z + d || x < -z - d) {
            // node's bounding sphere is either left or right of view frustum
            return false
        }

        return true
    }
}

class FrustumPlane {
    val upperLeft = MutableVec3f()
    val upperRight = MutableVec3f()
    val lowerLeft = MutableVec3f()
    val lowerRight = MutableVec3f()
}
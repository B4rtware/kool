package de.fabmax.kool.physics

import de.fabmax.kool.math.Mat4f
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.Vec4f
import physx.PxRigidActor
import physx.PxRigidBodyFlagEnum
import physx.PxRigidDynamic
import physx.globalPose

actual class RigidDynamic internal constructor(
    mass: Float,
    pose: Mat4f,
    isKinematic: Boolean,
    pxActor: PxRigidDynamic?
) : RigidBody() {

    actual constructor(mass: Float, pose: Mat4f, isKinematic: Boolean) : this(mass, pose, isKinematic, null)

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    protected val pxRigidDynamic: PxRigidDynamic
        get() = pxRigidActor as PxRigidDynamic

    override val pxRigidActor: PxRigidActor

    init {
        if (pxActor == null) {
            MemoryStack.stackPush().use { mem ->
                val pxPose = pose.toPxTransform(mem.createPxTransform())
                pxRigidActor = Physics.physics.createRigidDynamic(pxPose)
                this.mass = mass
            }
        } else {
            pxRigidActor = pxActor
        }
        if (isKinematic) {
            pxRigidBody.setRigidBodyFlag(PxRigidBodyFlagEnum.eKINEMATIC, true)
        }

        transform.set(pose)
    }

    actual fun wakeUp() {
        pxRigidDynamic.wakeUp()
    }

    actual fun putToSleep() {
        pxRigidDynamic.putToSleep()
    }

    actual fun setKinematicTarget(pose: Mat4f) {
        MemoryStack.stackPush().use { mem ->
            val pxPose = pose.toPxTransform(mem.createPxTransform())
            pxRigidDynamic.setKinematicTarget(pxPose)
        }
    }

    actual fun setKinematicTarget(position: Vec3f?, rotation: Vec4f?) {
        MemoryStack.stackPush().use { mem ->
            val pxPose = mem.createPxTransform()
            pxPose.p = position?.toPxVec3(mem.createPxVec3()) ?: pxRigidActor.globalPose.p
            pxPose.q = rotation?.toPxQuat(mem.createPxQuat()) ?: pxRigidActor.globalPose.q
            pxRigidDynamic.setKinematicTarget(pxPose)
        }
    }
}
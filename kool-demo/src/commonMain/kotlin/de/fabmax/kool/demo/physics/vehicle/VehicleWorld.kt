package de.fabmax.kool.demo.physics.vehicle

import de.fabmax.kool.physics.*
import de.fabmax.kool.physics.geometry.TriangleMeshGeometry
import de.fabmax.kool.physics.vehicle.VehicleUtils
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.IndexedVertexList
import de.fabmax.kool.util.deferred.DeferredPipeline
import de.fabmax.kool.util.deferred.deferredPbrShader

class VehicleWorld(
    val scene: Scene,
    val physics: PhysicsWorld,
    val deferredPipeline: DeferredPipeline
) {

    val defaultMaterial = Material(0.5f)
    val groundSimFilterData = FilterData(VehicleUtils.COLLISION_FLAG_GROUND, VehicleUtils.COLLISION_FLAG_GROUND_AGAINST)
    val groundQryFilterData = FilterData().apply { VehicleUtils.setupDrivableSurface(this) }
    val obstacleSimFilterData = FilterData(VehicleUtils.COLLISION_FLAG_DRIVABLE_OBSTACLE, VehicleUtils.COLLISION_FLAG_DRIVABLE_OBSTACLE_AGAINST)
    val obstacleQryFilterData = FilterData().apply { VehicleUtils.setupDrivableSurface(this) }

    fun toPrettyMesh(actor: RigidActor, meshColor: Color, rough: Float = 0.8f, metal: Float = 0f): Node = group {
        +colorMesh {
            generate {
                color = meshColor
                actor.shapes.forEach { shape ->
                    withTransform {
                        transform.mul(shape.localPose)
                        shape.geometry.generateMesh(this)
                    }
                }
            }
            shader = deferredPbrShader {
                roughness = rough
                metallic = metal
            }
            onUpdate += {
                this@group.transform.set(actor.transform)
                this@group.setDirty()
            }
        }
    }

    fun addStaticCollisionBody(mesh: IndexedVertexList) {
        val body = RigidStatic().apply {
            setSimulationFilterData(obstacleSimFilterData)
            setQueryFilterData(obstacleQryFilterData)
            attachShape(Shape(TriangleMeshGeometry(mesh), defaultMaterial))
        }
        physics.addActor(body)
    }

    fun release() {
        physics.clear()
        physics.release()
        defaultMaterial.release()
    }
}
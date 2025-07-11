package model.entities

import model.*
import model.managers.{EcosystemManager, MovableEntity}
import zio.{UIO, ZIO}

case class Sheep(id: EntityId.Type, position: Position, energy: Double = 50, mass: Int = 1300, speed: Double = 1.5) extends Entity with MovableEntity[Sheep]:

  def move(ecosystemManager: EcosystemManager): UIO[Unit] =
    for
      world <- ecosystemManager.getWorld  
      sheepOpt = world.sheepById(id)
      grassOpt = nearestGrass(id, world)
      _ <- (sheepOpt, grassOpt) match
        case (Some(sheep), Some(food)) =>
          val dx = food.position.x - sheep.position.x
          val dy = food.position.y - sheep.position.y
          val distance = math.hypot(dx, dy)
          if distance > 0 then
            val normalizedDx = dx / distance
            val normalizedDy = dy / distance
            ecosystemManager.moveEntityDirection(id, normalizedDx, normalizedDy)
          else
            ZIO.unit
        case _ => ZIO.unit
    yield ()
  
  def eat: Sheep =
    val gain = 10
    copy(energy = energy + gain)

  private def nearestGrass(sheep: EntityId.Type, world: World): Option[Grass] =
    world.grass
      .sortBy(grass => world.sheepById(sheep).map(s => s.position.distanceTo(grass)).getOrElse(Double.MaxValue))
      .headOption

  override def newPosition(newPos: Position): Sheep = this.copy(position = newPos)
package repositories

import datatables.{CategoryTable, EventTable, EventTriggerTable, UserTable}
import model._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class CategoryRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[Category](CategoryTable.table) {

  def createSchema(): Future[Unit] = {
    db.run((
      UserTable.table.schema ++
      CategoryTable.table.schema ++
      EventTable.table.schema ++
      EventTriggerTable.table.schema).create
    )
  }

  def dropSchema(): Future[Unit] = {
    db.run((
      UserTable.table.schema ++
      CategoryTable.table.schema ++
      EventTable.table.schema ++
      EventTriggerTable.table.schema).drop
    )
  }

  def findAll(): Future[Vector[model.Category]] = db.run(CategoryTable.table.to[Vector].result)

}

class UserRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[User](UserTable.table) {

  def isEmailAvailable(email: String): Future[Boolean] =
    db.run(
      UserTable.table
        .filter(_.email === email)
        .exists
        .result
    )

  def loginProcess(email: String, password: String): Future[Option[String]] =
    db.run(
      UserTable.table
        .filter(_.email === email)
        .filter(_.password === password)
        .map(_.token)
        .result
        .headOption
    )

  def updateToken(oldToken: String, newToken: String) =
    db.run(
      UserTable.table
        .filter(_.token === oldToken)
        .map(_.token)
        .update(newToken)
    )

  def findIdByToken(token: String) =
    db.run(
      UserTable.table
        .filter(_.token === token)
        .map(_.id)
        .result
        .headOption
    )

  def findUserInfoByPosition(latitude: Double, longitude: Double) =
    db.run(
      EventTable.table
        .filter(it => it.latitude === latitude && it.longitude === longitude)
        .join(UserTable.table)
        .on(_.userId === _.id)
        .map(_._2)
        .result
        .headOption
    )

}

class EventRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[Event](EventTable.table) {

  def findAll: Future[Vector[model.Event]] = db.run(EventTable.table.to[Vector].result)

  def findAllByCategory(category: Int): Future[Vector[model.Event]] =
    db.run(
      EventTable.table
        .filter(_.categoryId === category)
        .to[Vector]
        .result
    )

  def findAllByToken(token: String): Future[Vector[model.Event]] =
    db.run(
      UserTable.table
        .filter(_.token === token)
        .join(EventTable.table)
        .on(_.id === _.userId)
        .map(_._2)
        .to[Vector]
        .result
    )

  def findAllByCategoryAndToken(category: Int, token: String): Future[Vector[model.Event]] =
    db.run(
      UserTable.table
        .filter(_.token === token)
        .join(EventTable.table)
        .on(_.id === _.userId)
        .map(_._2)
        .filter(_.categoryId === category)
        .to[Vector]
        .result
    )

  def findByIdAndToken(eventId: Int, token: String) =
    db.run(
      UserTable.table
        .filter(_.token === token)
        .join(EventTable.table)
        .on(_.id === _.userId)
        .map(_._2)
        .filter(_.id === eventId)
        .map(_.id)
        .result
        .headOption
    )
}

class EventTriggerRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[EventTrigger](EventTriggerTable.table) {

  private val distanceCalculator = new DistanceCalculator

  def findAllMy(token: String): Future[Vector[EventTrigger]] =
    db.run(
      EventTriggerTable.table
        .join(UserTable.table)
        .on(_.userId === _.id)
        .filter(_._2.token === token)
        .map(_._1)
        .to[Vector]
        .result
    )

  def getNotMyTriggers(event: EventModelIn): Future[Seq[EventTrigger]] =
    for {
      result <- db.run(
        EventTriggerTable.table
          .join(UserTable.table)
          .on(_.userId === _.id)
          .filter(_._2.token =!= event.token)
          .map(_._1)
          .result
      )
    } yield {
      result.filter(it => distanceCalculator.isLocationInRadius(
        Location(it.latitude, it.longitude),
        Location(event.latitude, event.longitude),
        it.radius
      ))
    }

  def findByIdAndToken(eventTriggerId: Int, token: String): Future[Option[Int]] =
    db.run(
      EventTriggerTable.table
        .join(UserTable.table)
        .on(_.userId === _.id)
        .filter(_._2.token === token)
        .map(_._1.id)
        .filter(_ === eventTriggerId)
        .result
        .headOption
    )
}
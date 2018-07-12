package datatables

import model.EventTrigger
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class EventTriggerTable(tag: Tag) extends Table[EventTrigger](tag, "event_trigger") with BaseTable[EventTrigger] {
  val id = column[Int]("trigger_id", O.PrimaryKey, O.AutoInc)
  val userId = column[Int]("user_id")
  val latitude = column[Double]("trigger_latitude")
  val longitude = column[Double]("trigger_longitude")
  val radius = column[Int]("trigger_radius")
  val pushId = column[String]("push_id")

  val userIdForeignKey = foreignKey(
    "user_id_fk", userId, UserTable.table)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  def * = (id, userId, latitude, longitude, radius, pushId).mapTo[EventTrigger]
}

object EventTriggerTable {
  val table = TableQuery[EventTriggerTable]
}

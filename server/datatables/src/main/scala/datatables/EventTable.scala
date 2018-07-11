package datatables

import java.time.LocalDateTime

import model.Event
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class EventTable(tag: Tag) extends Table[model.Event](tag, "event") with BaseTable[Event] {
  val id = column[Int]("event_id", O.PrimaryKey, O.AutoInc)
  val name = column[String]("name")
  val categoryId = column[Int]("cat_id")
  val startTime = column[LocalDateTime]("start_time")
  val endTime = column[LocalDateTime]("end_time")
  val latitude = column[Double]("latitude")
  val longitude = column[Double]("longitude")
  val userId = column[Int]("user_id")

  val categoryIdForeignKey = foreignKey(
    "category_id_fk", categoryId, CategoryTable.table)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  val userIdForeignKey = foreignKey(
    "user_id_fk", userId, UserTable.table)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  def * = (id, name, categoryId, startTime, endTime, latitude, longitude, userId).mapTo[model.Event]
}

object EventTable {
  val table = TableQuery[EventTable]
}

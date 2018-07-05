package datatables

import model.Category
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class CategoryTable(tag: Tag) extends Table[Category](tag, "category") with BaseTable[Category] {
  val id = column[Int]("category_id", O.PrimaryKey, O.AutoInc)
  val name = column[String]("event_name")

  def * = (id, name).mapTo[Category]
}

object CategoryTable {
  val table = TableQuery[CategoryTable]
}

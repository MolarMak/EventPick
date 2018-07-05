package datatables

import model.User
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class UserTable(tag: Tag) extends Table[User](tag, "user") with BaseTable[User] {
  val id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
  val firstName = column[String]("user_fname")
  val lastName = column[Option[String]]("user_lname")
  val age = column[Int]("user_age")
  val gender = column[Int]("user_gender")
  val email = column[String]("user_email")
  val password = column[String]("user_password")
  val token = column[String]("user_token")

  def * = (id, firstName, lastName, age, gender, email, password, token).mapTo[User]
}

object UserTable {
  val table = TableQuery[UserTable]
}

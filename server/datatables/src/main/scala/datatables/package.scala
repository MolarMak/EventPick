import java.time.LocalDate

import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

package object datatables {

  implicit val localDateToDateMapper: JdbcType[LocalDate] =
    MappedColumnType.base[LocalDate, java.sql.Date](java.sql.Date.valueOf, _.toLocalDate)

}

import java.time.{LocalDate, LocalDateTime}

import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

package object datatables {

  implicit val localDateToDateMapper: JdbcType[LocalDate] =
    MappedColumnType.base[LocalDate, java.sql.Date](java.sql.Date.valueOf, _.toLocalDate)

  implicit val localDateTimeToDateMapper: JdbcType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, java.sql.Timestamp](java.sql.Timestamp.valueOf, _.toLocalDateTime)

}

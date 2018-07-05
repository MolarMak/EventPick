package server

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}

import model.{Category, Event, User}

object SeedData {
  val categories = Set(
    Category(1, "Sport"),
    Category(2, "Tourism"),
    Category(3, "Communication"),
    Category(4, "Party"),
    Category(5, "Education"),
    Category(6, "Meeting")
  )

  val users = Set(
    User(1, "Maxim", Some("Mamuta"), 19, 1, "molarmak@gmail.com", "12345qwerty", "fsdfsgdfhg123"),
    User(2, "Alex", None, 20, 1, "alex.alex@gmail.com", "qwerty12345", "sdgdbsgkljag124")
  )

  val events = Set(
    Event(1, "Sport fest", 1, Timestamp.valueOf(LocalDateTime.of(2018, 6, 28, 10, 10)), Timestamp.valueOf(LocalDateTime.of(2018, 6, 30, 10, 30)), 50.447773, 30.452274, 1)
  )
}

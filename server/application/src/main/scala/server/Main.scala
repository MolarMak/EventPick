package server

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import repositories.{CategoryRepository, EventRepository, EventTriggerRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    val db = Database.forConfig("postgresql")

    implicit val system = ActorSystem("event-bus-system")
    implicit val materializer = ActorMaterializer()
    val http: HttpExt = Http()

    val categoryRepository = new CategoryRepository(db)
    val userRepository = new UserRepository(db)
    val eventRepository = new EventRepository(db)
    val eventTriggerRepository = new EventTriggerRepository(db)
    val eventPickApi = new EventPickApi(categoryRepository, userRepository, eventRepository, eventTriggerRepository, http)

    val bindingFuture = http.bindAndHandle(eventPickApi.routes, "0.0.0.0", 8080)

    scala.io.StdIn.readLine("Server is up")
    val stop = for {
      binding <- bindingFuture
      _ <- binding.unbind()
      _ <- system.terminate()
    } yield ()
    Await.result(stop, 1.second)
    db.close()
  }

  private def remakeData(categoryRepository: CategoryRepository): Unit = {
    categoryRepository.createSchema()
    categoryRepository.insert(SeedData.categories.toSeq)
  }
}

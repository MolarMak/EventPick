package server

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.syntax._
import model._
import repositories.{CategoryRepository, EventRepository, UserRepository}

import scala.util.{Failure, Success}

class EventPickApi(categoryRepository: CategoryRepository,
                   userRepository: UserRepository,
                   eventRepository: EventRepository) extends Directives with FailFastCirceSupport {

  val apiVersion = "v1"
  val isLogged = true

  def getCategories: Route =
    path("api" / apiVersion / "getCategories") {
      val categoryFuture = categoryRepository.findAll()
      onSuccess(categoryFuture) { categories =>
          complete(CategoryModel(categories = categories.map(_.name)).asJson)
      }
    }

  def login: Route =
    path("api" / apiVersion / "login") {
      post {
        entity(as[LoginModel]) { login =>
          log("login", s"input: ${login.toString}")
          val loginFuture = userRepository.loginProcess(login.email, login.password)
          onComplete(loginFuture) {
            case Success(token: Some[String]) => complete(TokenResponseTrue(token = token.get).asJson)
            case _ => complete(responseFalse("Email or password incorrect"))
          }

        }
      }
    }

  def logout: Route =
    path("api" / apiVersion / "logout") {
      parameters('token) { token =>
        log("logout", s"input: $token")
        val newToken = generateToken()
        val updateTokenFuture = userRepository.updateToken(token, newToken)
        onComplete(updateTokenFuture) {
          case Success(1) => complete(responseTrue())
          case _ => complete(responseFalse("Can't find user with this id"))
        }
      }
    }

  def register: Route =
    path("api" / apiVersion / "register") {
      post {
        entity(as[RegisterModel]) { register =>
          log("register", s"input: ${register.toString}")

          val validation = new Validation().registerValidate(register.firstName, register.gender, register.email, register.password)
          if(validation.nonEmpty) {
            complete(responseFalseSeveral(validation))
          } else {

            val token = generateToken()

            val user = User(1,
              register.firstName,
              register.lastName,
              register.age,
              register.gender,
              register.email,
              register.password,
              token)

            val isAvailable = userRepository.isEmailAvailable(register.email)

            onComplete(isAvailable) {
              case Success(true) => complete(responseFalse("Email already exist!"))

              case Success(false) =>
                val insertFuture = userRepository.insert(user)
                onComplete(insertFuture) {
                  case Success(1) => complete(TokenResponseTrue(token = token).asJson)
                  case _ => complete(responseFalse("Error when process registration!"))
                }

              case _ => complete(responseFalse("Error when process registration!"))
            }
          }
        }
      }
    }

  def createEvent: Route =
    path("api" / apiVersion / "createEvent") {
      post {
        entity(as[EventModelIn]) { event =>
          log("createEvent", s"input: $event")

          val findUserTokenFuture = userRepository.findIdByToken(event.token)
          onComplete(findUserTokenFuture) {

            case Success(id:Some[Int]) =>
              try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val eventData = Event(1,
                  event.name,
                  event.categoryId,
                  Timestamp.valueOf(LocalDateTime.parse(event.startTime, formatter)),
                  Timestamp.valueOf(LocalDateTime.parse(event.endTime, formatter)),
                  event.latitude,
                  event.longitude,
                  id.get
                )

                val validation = new Validation().createEventValidate(eventData.name, eventData.startTime, eventData.endTime)
                if(validation.nonEmpty) {
                  complete(responseFalseSeveral(validation))
                } else {
                  val insertEventFuture = eventRepository.insert(eventData)
                  onComplete(insertEventFuture) {
                    case Success(1) => complete(responseTrue())
                    case _ => complete(responseFalse("Can't create event"))
                  }
                }
              } catch {
                case _: DateTimeParseException => complete(responseFalse("Can't parse date"))
                case _: Throwable => complete(responseFalse("Can't create event"))
              }

            case _ => complete(responseFalse("Not valid token"))
          }


        }
      }
    }

  def deleteEvent: Route =
    path("api" / apiVersion / "deleteEvent") {
      delete {
        entity(as[DeleteEventModel]) { deleteEvent =>
          log("deleteEvent", s"input: $deleteEvent")
          val findFuture = eventRepository.findByIdAndToken(deleteEvent.eventId, deleteEvent.token)
          onComplete(findFuture) {
            case Success(Some(id)) =>
              val deleteFuture = eventRepository.deleteById(id)
              onComplete(deleteFuture) {
                case Success(1) => complete(DeleteEventResponse(eventId = id).asJson)
                case _ => complete(responseFalse("Can't delete event"))
              }

            case _ => complete(responseFalse("Can't find token or id"))
          }
        }
      }
    }

  def getEvents: Route =
    path("api" / apiVersion / "getEvents") {
      parameters('category.as[Int].?, 'token.?) { (eventCategory, tokenCategory) =>
        val futureGetEvents = (eventCategory, tokenCategory) match {
          case (Some(category), None) => eventRepository.findAllByCategory(category)
          case (None, Some(token)) => eventRepository.findAllByToken(token)
          case (Some(category), Some(token)) => eventRepository.findAllByCategoryAndToken(category, token)
          case (None, None) => eventRepository.findAll
        }
        onComplete(futureGetEvents) {
          case Success(events) => complete(EventsModel(events = events.map(eventToDataType)).asJson)
          case Failure(_) => complete(responseFalse("Can't get events"))
        }
      }
    }

  def getUserInfo: Route =
    path("api" / apiVersion / "getUserInfo") {
      parameter('latitude.as[Double], 'longitude.as[Double]) { (latitude, longitude) =>
        val findUserFuture = userRepository.findUserInfoByPosition(latitude, longitude)
        onComplete(findUserFuture) {
          case Success(user : Some[User]) =>
            val response = UserInfoModel(
              latitude,
              longitude,
              user.get.firstName,
              user.get.lastName,
              user.get.email)

            complete(UserInfoResponse(info = response).asJson)

          case _ => complete(responseFalse("Can't find user by location"))
        }
      }
    }

  def routes: Route = getCategories ~ register ~ login ~ logout ~ createEvent ~ getEvents ~ deleteEvent ~ getUserInfo

  def responseTrue(): Json = ResponseTrue().asJson

  def responseFalse(error: String): Json = ResponseFalse(errors = List(error)).asJson

  def responseFalseSeveral(errors: List[String]): Json = ResponseFalse(errors = errors).asJson

  def log(methodName: String, message: String) = {
    if(isLogged) {
      println(s"$methodName => $message")
    }
  }

  def generateToken(): String = {
    val random = new scala.util.Random

    def randomString(alphabet: String)(n: Int): String =
      Stream.continually(random.nextInt(alphabet.length)).map(alphabet).take(n).mkString

    def randomAlphanumericString(n: Int) =
      randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)

    randomAlphanumericString(32)
  }

  def eventToDataType(event: Event): EventModelOut = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    EventModelOut(
      event.id,
      event.name,
      event.categoryId,
      formatter.format(event.startTime.toLocalDateTime),
      formatter.format(event.endTime.toLocalDateTime),
      event.latitude,
      event.longitude,
      event.userId
    )
  }

}

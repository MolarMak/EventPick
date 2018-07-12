package server

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import akka.actor.ActorSystem
import akka.http.javadsl.Http
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, RawHeader}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.syntax._
import model._
import repositories._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class EventPickApi(categoryRepository: CategoryRepository,
                   userRepository: UserRepository,
                   eventRepository: EventRepository,
                   eventTriggerRepository: EventTriggerRepository,
                   http: HttpExt) extends Directives with FailFastCirceSupport {

  private val apiVersion = "v1"
  private val isLogged = true
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val cmToken = ConfigFactory.load().getString("credentials.cm_token")

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
            case Success(Some(token)) => complete(TokenResponseTrue(token = token).asJson)
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

            case Success(Some(id)) =>
              try {
                val eventData = Event(1,
                  event.name,
                  event.categoryId,
                  LocalDateTime.parse(event.startTime, formatter),
                  LocalDateTime.parse(event.endTime, formatter),
                  event.latitude,
                  event.longitude,
                  id
                )

                val validation = new Validation().createEventValidate(eventData.name, eventData.startTime, eventData.endTime)
                if(validation.nonEmpty) {
                  complete(responseFalseSeveral(validation))
                } else {
                  val insertEventFuture = eventRepository.insert(eventData)
                  onComplete(insertEventFuture) {
                    case Success(1) =>
                      sendNotification(event)
                      complete(responseTrue())

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

  def createEventTrigger: Route =
    path("api" / apiVersion / "createEventTrigger") {
      post {
        entity(as[EventTriggerModelIn]) { eventTrigger =>
          log("createEventTrigger", s"input: $eventTrigger")

          val findUserTokenFuture = userRepository.findIdByToken(eventTrigger.token)

          onComplete(findUserTokenFuture) {
            case Success(Some(id)) =>
              val eventTriggerData = EventTrigger(1,
                id,
                eventTrigger.latitude,
                eventTrigger.longitude,
                eventTrigger.radius,
                eventTrigger.pushId
              )

              val insertEventTriggerFuture = eventTriggerRepository.insert(eventTriggerData)
              onComplete(insertEventTriggerFuture) {
                case Success(1) => complete(responseTrue())
                case _ => complete(responseFalse("Can't create event trigger"))
              }

            case _ => complete(responseFalse("Not valid token"))
          }
        }
      }
    }

  def deleteEventTrigger: Route =
    path("api" / apiVersion / "deleteEventTrigger") {
      delete {
        entity(as[DeleteEventTrigger]) { deleteEventTrigger =>
          log("deleteEventTrigger", s"input: $deleteEventTrigger")

          val findFuture = eventTriggerRepository.findByIdAndToken(deleteEventTrigger.eventTriggerId, deleteEventTrigger.token)
          onComplete(findFuture) {
            case Success(Some(id)) =>
              val deleteEventTriggerFuture = eventTriggerRepository.deleteById(id)
              onComplete(deleteEventTriggerFuture) {
                case Success(1) => complete(DeleteEventTriggerResponse(eventTriggerId = id).asJson)
                case _ => complete(responseFalse("Can't delete trigger"))
              }

            case _ => complete(responseFalse("Token or trigger id not valid"))
          }
        }
      }
    }

  def getEventTriggers: Route =
    path("api" / apiVersion / "getEventTriggers") {
      parameters('token) { token =>
        val findAllEventTriggersFuture = eventTriggerRepository.findAllMy(token)
        onSuccess(findAllEventTriggersFuture) { eventTriggers =>
          complete(EventTriggersModel(eventTriggers = eventTriggers.map(eventTriggerOut)).asJson)
        }
      }
    }

  def getUserInfo: Route =
    path("api" / apiVersion / "getUserInfo") {
      parameter('latitude.as[Double], 'longitude.as[Double]) { (latitude, longitude) =>
        val findUserFuture = userRepository.findUserInfoByPosition(latitude, longitude)
        onComplete(findUserFuture) {
          case Success(Some(user)) =>
            val response = UserInfoModel(
              latitude,
              longitude,
              user.firstName,
              user.lastName,
              user.email)

            complete(UserInfoResponse(info = response).asJson)

          case _ => complete(responseFalse("Can't find user by location"))
        }
      }
    }

  def routes: Route =
      getCategories ~
      register ~
      login ~
      logout ~
      createEvent ~
      getEvents ~
      deleteEvent ~
      getUserInfo ~
      createEventTrigger ~
      getEventTriggers ~
      deleteEventTrigger

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

  def sendNotification(event: EventModelIn): Unit = {
    val getTriggers = eventTriggerRepository.getNotMyTriggers(event)

    getTriggers.onComplete {
      case Success(triggers) =>
        log("sendNotification", triggers.toString())
        val notification = Notification(body = s"${event.name} event created in your notification zone!")
        triggers.foreach{ it =>
          val notificationSend = NotificationSend(it.pushId, notification)
          val request = HttpRequest(
            HttpMethods.POST,
            "https://fcm.googleapis.com/fcm/send",
            entity = HttpEntity(
              ContentTypes.`application/json`,
              notificationSend.asJson.noSpaces)
          ).withHeaders(RawHeader("Authorization", s"key=$cmToken"))
          val response = http.singleRequest(request)
          response.onComplete {
            case Success(httpResponse) => log("sendNotification", s"response: $httpResponse")
            case Failure(httpResponse) => log("sendNotification", s"failure response: $httpResponse")
            case _ => log("sendNotification", s"unknown failure")
          }
          log("sendNotification", s"response: $response")
        }


      case Failure(t) => log("sendNotication", "An error has occurred: " + t.getMessage)
    }
  }

  def eventToDataType(event: Event): EventModelOut = {
    EventModelOut(
      event.id,
      event.name,
      event.categoryId,
      formatter.format(event.startTime),
      formatter.format(event.endTime),
      event.latitude,
      event.longitude,
      event.userId
    )
  }

  def eventTriggerOut(eventTrigger: EventTrigger): EventTriggerModelOut = {
    EventTriggerModelOut(
      eventTrigger.id,
      eventTrigger.latitude,
      eventTrigger.longitude,
      eventTrigger.radius
    )
  }

}

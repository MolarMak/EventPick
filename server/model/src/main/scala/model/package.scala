import java.time.LocalDateTime

import io.circe.generic.JsonCodec

package object model {

  trait HasId {val id: Int}

  case class User(id: Int, firstName: String, lastName: Option[String], age: Int, gender: Int, email: String, password: String, token: String) extends HasId

  case class Event(id: Int, name: String, categoryId: Int, startTime: LocalDateTime, endTime: LocalDateTime, latitude: Double, longitude: Double, userId: Int) extends HasId

  case class Category(id: Int, name: String) extends HasId

  case class EventTrigger(id: Int, userId: Int, latitude: Double, longitude: Double, radius: Int, pushId: String) extends HasId

  @JsonCodec
  case class ResponseFalse(result: Boolean = false, errors: List[String])

  @JsonCodec
  case class ResponseTrue(result: Boolean = true)

  @JsonCodec
  case class RegisterModel(firstName: String, lastName: Option[String], age: Int, gender: Int, email: String, password: String)

  @JsonCodec
  case class LoginModel(email: String, password: String)

  @JsonCodec
  case class TokenResponseTrue(result: Boolean = true, token: String)

  @JsonCodec
  case class EventModelIn(name: String, categoryId: Int, startTime: String, endTime: String, latitude: Double, longitude: Double, token: String)

  @JsonCodec
  case class EventModelOut(id: Int, name: String, categoryId: Int, startTime: String, endTime: String, latitude: Double, longitude: Double, userId: Int)

  @JsonCodec
  case class DeleteEventModel(eventId: Int, token: String)

  @JsonCodec
  case class DeleteEventResponse(result: Boolean = true, eventId: Int)

  @JsonCodec
  case class CategoryModel(result: Boolean = true, categories: Vector[String])

  @JsonCodec
  case class EventsModel(result: Boolean = true, events: Vector[EventModelOut])

  @JsonCodec
  case class UserInfoModel(latitude: Double, longitude: Double, userFirstName: String, userLastName: Option[String], email: String)

  @JsonCodec
  case class UserInfoResponse(result: Boolean = true, info: UserInfoModel)

  @JsonCodec
  case class EventTriggerModelIn(token: String, latitude: Double, longitude: Double, radius: Int, pushId: String)

  @JsonCodec
  case class EventTriggerModelOut(id: Int, latitude: Double, longitude: Double, radius: Int)

  @JsonCodec
  case class EventTriggersModel(result: Boolean = true, eventTriggers: Vector[EventTriggerModelOut])

  @JsonCodec
  case class Notification(body: String, content_available: Boolean = true, priority: String = "high")

  @JsonCodec
  case class NotificationSend(to: String, notification: Notification)

  @JsonCodec
  case class DeleteEventTrigger(eventTriggerId: Int, token: String)

  @JsonCodec
  case class DeleteEventTriggerResponse(result: Boolean = true, eventTriggerId: Int)
}

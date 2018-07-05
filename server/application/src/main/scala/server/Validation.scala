package server

import java.sql.Timestamp

class Validation {

  private sealed trait DomainValidation {
    def errorMessage: String
  }

  private case object NameRuleValidation extends DomainValidation {
    def errorMessage: String = "Name length must be between 2 and 30 characters"
  }

  private case object GenderValidation extends DomainValidation {
    def errorMessage: String = "Gender can only be male or female"
  }

  private case object EmailValidation extends DomainValidation {
    def errorMessage: String = "Incorrect email format"
  }

  private case object PasswordValidation extends DomainValidation {
    def errorMessage: String = "Password length must at least 8 characters"
  }

  private case object EventNameValidation extends DomainValidation {
    def errorMessage: String = "Event name length must be between 2 and 30 characters"
  }

  private case object TimeValidation extends DomainValidation {
    def errorMessage: String = "The initial time must be before the final"
  }

  private def nameRule(name: String) = name.length > 2 && name.length < 30
  private def genderRule(gender: Int) = gender == 1 || gender == 2
  private def emailRule(email: String) = """\A([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})\z""".r.findFirstMatchIn(email).isDefined
  private def passwordRule(password: String) = password.length >= 8
  private def eventNameRule(name: String) = name.length > 2 && name.length < 60
  private def startEndTimeValidate(startTime: Timestamp, endTime: Timestamp) = startTime.before(endTime)

  private def validate(rules: List[(Boolean, DomainValidation)]) : List[String] =
    rules.foldLeft(List[Option[String]]()) {
      case (seq, (rule, validator)) => seq :+ validationStage(rule, validator)
    }.flatten

  private def validationStage(rule: Boolean, domainValidation: DomainValidation): Option[String] =
    if (!rule) Some(domainValidation.errorMessage) else None

  def registerValidate(firstName: String, gender: Int, email: String, password: String): List[String] = {
    validate(List(
      (nameRule(firstName), NameRuleValidation),
      (genderRule(gender), GenderValidation),
      (emailRule(email), EmailValidation),
      (passwordRule(password), PasswordValidation)
    ))
  }

  def createEventValidate(name: String, startTime: Timestamp, endTime: Timestamp): List[String] = {
    validate(List(
      (eventNameRule(name), EventNameValidation),
      (startEndTimeValidate(startTime, endTime), TimeValidation)
    ))
  }
}

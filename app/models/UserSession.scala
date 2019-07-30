package models


import java.time.LocalDateTime

import mongoDateTimeFormats.MongoDateTimeFormats
import play.api.libs.json._

case class UserSession(_id:String, lastUpdated:LocalDateTime)

object UserSession extends MongoDateTimeFormats{
  implicit lazy val format: OFormat[UserSession] = Json.format
}
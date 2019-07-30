package models

import play.api.libs.json.{Json, OFormat}
import play.api.mvc.PathBindable

case class Card(_id: String)

object Card {

  implicit val pathBindable: PathBindable[Card] = {
    new PathBindable[Card] {
      override def bind(key: String, value: String): Either[String, Card] = {
        if (value.matches("^[a-zA-Z0-9]+$")) {
          Right(Card(value))
        } else {
          Left("Invalid Card Id")
        }
      }

      override def unbind(key: String, value: Card): String = {
        value._id
      }
    }
  }
  implicit lazy val format: OFormat[Card] = Json.format
}



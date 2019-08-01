
package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Members(
                    _id: Card,
                    name: String,
                    email: String,
                    mobileNumber: String,
                    balance: Int,
                    securityNumber: Int
                  )


object Members {

  implicit val reads: Reads[Members] = (
    __.read[Card] and
      (__ \ "name").read[String] and
      (__ \ "email").read[String] and
      (__ \ "mobileNumber").read[String] and
      (__ \ "balance").read[Int] and
      (__ \ "securityNumber").read[Int]
    ) (Members.apply _)

  implicit val writes: OWrites[Members] = (
    __.write[Card] and
      (__ \ "name").write[String] and
      (__ \ "email").write[String] and
      (__ \ "mobileNumber").write[String] and
      (__ \ "balance").write[Int] and
      (__ \ "securityNumber").write[Int]
    ) (unlift(Members.unapply))
}
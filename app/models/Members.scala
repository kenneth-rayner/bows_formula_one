
package models

import play.api.libs.json._

case class Members(_id:String, name: String, email:String, mobileNumber: String, balance: Int, securityNumber:Int)


object Members {
  implicit lazy val format: OFormat[Members] = Json.format
}
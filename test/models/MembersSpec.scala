package models

import org.scalatest._
import play.api.libs.json.Json

class MembersSpec extends WordSpec with OptionValues with MustMatchers {

  val card: Card = Card("id")

  "Member" must {
    "Deserialize correctly" in {

      val json = Json.obj(
        "_id" -> "id",
        "name" -> "Fred",
        "email" -> "a@b.com",
        "mobileNumber" -> "07444345",
        "balance" -> 10000,
        "securityNumber" -> 1234
      )

      val expectedMember = Members(
        _id = card,
        name = "Fred",
        email = "a@b.com",
        mobileNumber = "07444345",
        balance = 10000,
        securityNumber = 1234

      )
      json.as[Members] mustEqual expectedMember

    }
    "Serialize correctly" in {
      val member = Members(
        _id = card,
        name = "Fred",
        email = "a@b.com",
        mobileNumber = "07444345",
        balance = 10000,
        securityNumber = 1234
      )

      val expectedJson = Json.obj(
        "_id" -> "id",
        "name" -> "Fred",
        "email" -> "a@b.com",
        "mobileNumber" -> "07444345",
        "balance" -> 10000,
        "securityNumber" -> 1234


      )
      Json.toJson(member) mustBe expectedJson
    }
  }
}

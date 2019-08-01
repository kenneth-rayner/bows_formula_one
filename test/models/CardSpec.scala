package models

import models.Card.pathBindable
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json

class CardSpec extends WordSpec with OptionValues with MustMatchers {

  "Card" must {

    val validCard = "dssfd123"

    "Deserialise" in {
      val cardId = Card(
        _id = validCard
      )
      val expectedJson = Json.obj(
        "_id" -> validCard
      )
      Json.toJson(cardId) mustEqual expectedJson

    }
    "Serialise" in {
      val expectedCardId = Card(
        _id = validCard
      )
      val json = Json.obj(
        "_id" -> validCard
      )
      json.as[Card] mustEqual expectedCardId
    }
    "return 'Invalid card id' if _id does not match regex" in {

      val invalidCard = "!dssfd123"
      val result = "Invalid Card Id"

      pathBindable.bind("", invalidCard) mustBe Left(result)

    }
    "return a string" in {

      pathBindable.unbind("", Card("testId")) mustEqual "testId"
    }
  }
}

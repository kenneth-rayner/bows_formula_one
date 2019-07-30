package models

import org.scalatest.{FreeSpec, MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json

class Card_IdSpec extends WordSpec with OptionValues with MustMatchers {

  "Card" must {

  val validCard = "dssfd123"

    "bind correct data" in {
     val cardId = Card(
       _id = validCard
     )
      val expectedJson  = Json.obj(
        "_id" -> validCard
      )
        Json.toJson(cardId) mustEqual expectedJson

    }
    "unbind correct data" in {
      val expectedCardId = Card(
        _id = validCard
      )
      val json = Json.obj(
        "_id" -> validCard
      )
      json.as[Card]mustEqual expectedCardId
    }
  }
 }

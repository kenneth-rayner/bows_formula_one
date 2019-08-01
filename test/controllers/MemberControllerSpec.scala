package controllers

import java.time.LocalDateTime

import repositories.{MemberRepository, SessionRepository}
import models.{Card, Members, UserSession}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.core.errors.DatabaseException

import scala.concurrent.Future

class MemberControllerSpec extends WordSpec with MustMatchers
  with MockitoSugar with ScalaFutures {

  val mockMemberRespository: MemberRepository = mock[MemberRepository]
  val mockSessionRespository: SessionRepository = mock[SessionRepository]

  private lazy val builder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().overrides(
      bind[MemberRepository].toInstance(mockMemberRespository),
      bind[SessionRepository].toInstance(mockSessionRespository)
    )


  "present" must {

    "return ok and delete session if one already exists" in {
      when(mockMemberRespository.getMemberById(any()))
        .thenReturn(Future.successful(Some(Members("testId", "testName", "testEmail", "testMobile", 123, 123))))

      when(mockSessionRespository.getSession(any()))
        .thenReturn(Future.successful(Some(UserSession("testId", LocalDateTime.now))))

      when(mockSessionRespository.deleteSessionById(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.present(Card("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Goodbye testName"

      app.stop
    }

    "return ok and create new session if none exist" in {
      when(mockMemberRespository.getMemberById(any()))
        .thenReturn(Future.successful(Some(Members("testId", "testName", "testEmail", "testMobile", 123, 123))))

      when(mockSessionRespository.getSession(any()))
        .thenReturn(Future.successful(None))

      when(mockSessionRespository.createNewSession(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.present(Card("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Hello testName"

      app.stop
    }

    "return BadRequest if member does not exist" in {
      when(mockMemberRespository.getMemberById(any()))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.present(Card("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Please register"

      app.stop
    }

    "return BadRequest if data in mongo is invalid" in {
      when(mockMemberRespository.getMemberById(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.present(Card("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Could not parse Json to Members model. Incorrect data!"

      app.stop
    }

    "return BadRequest if something else has failed" in {
      when(mockMemberRespository.getMemberById(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.present(Card("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }

    "getmemberById" must {
      "return ok and members details" in {
        when(mockMemberRespository.getMemberById(any()))
          .thenReturn(Future.successful(Some(Members("testId", "testName", "testEmail", "testMobile", 123, 123))))
        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.getMemberById(Card("testId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) must contain
        """{
            "_id":"testId","name":testName,"email":"testEmail",
            "mobileNumber":"testMobile","balance":123,"securityNumber":123}""".stripMargin

        app.stop
      }
      "return 'member' not found' when id not present with status 404" in {
        when(mockMemberRespository.getMemberById(any()))
          .thenReturn(Future.successful(None))
        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.getMemberById(Card("wrongId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe 404
        contentAsString(result) mustBe "Member not found"

        app.stop
      }
    }
    "getBalance" must {
      "return NOT_FOUND and correct error message when invalid request input" in {

        when(mockMemberRespository.getMemberById(any()))
          .thenReturn(Future.successful(None))

        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController.getBalance
        (Card("testId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe "Member not found!"

        app.stop

      }
      "return correct balance and status ok when correct request input" in {

        when(mockMemberRespository.getMemberById(any()))
          .thenReturn(Future.successful(Some(Members("testId", "testName", "testEmail", "testMobile", 123, 123))))

        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MemberController
          .getBalance(Card("testId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "123"

        app.stop

      }
    }

    "addNewMember" must {

      "return 'success if valid data is input" in {

        when(mockMemberRespository.addNewMember(any()))
          .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

        val membersJson: JsValue = Json.toJson(Members("test", "test", "test", "test", 123, 123))

        val app: Application = builder.build()

        val request: FakeRequest[JsValue] =
          FakeRequest(POST, routes.MemberController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Success"

        app.stop

      }

      "Return BAD_REQUEST and correct error message when invalid data is input" in {

        val membersJson: JsValue = Json.toJson("Invalid Json")

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, routes.MemberController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Could not parse Json to Member model. Incorrect data!"

        app.stop

      }

      "Return BAD_REQUEST and correct error message when duplicate data is input" in {

        when(mockMemberRespository.addNewMember(any()))
          .thenReturn(Future.failed(new DatabaseException {
            override def originalDocument: Option[BSONDocument] = None

            override def code: Option[Int] = None

            override def message: String = "Duplicate key"
          }))

        val membersJson: JsValue = Json.toJson(Members("test", "test", "test", "test", 123, 123))

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, routes.MemberController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Could not parse Json to Member model. Duplicate key error!"

        app.stop

      }

      "Return BAD_REQUEST and correct error message for any other fault" in {

        when(mockMemberRespository.addNewMember(any()))
          .thenReturn(Future.failed(new Exception))

        val membersJson: JsValue = Json.toJson(Members("test", "test", "test", "test", 123, 123))

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, routes.MemberController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

        app.stop

      }
    }

    "deleteMember" must {
      "Return Ok and correct error message when valid data is input" in {
        when(mockMemberRespository.deleteMemberById(any()))
          .thenReturn(Future.successful(Some(Json.obj(
            "_id" -> "testId",
            "name" -> "testName",
            "email" -> "testEmail",
            "mobileNumber" -> "testNumber",
            "balance" -> 123,
            "securityNumber" -> 123
          ))))

        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.MemberController.deleteMember(Card("testId")).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Success"

        app.stop

      }
    }
    "Return Not found and correct error message when not found data" in {
      when(mockMemberRespository.deleteMemberById(any()))
        .thenReturn(Future.successful(None
        ))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, routes.MemberController.deleteMember(Card("testId")).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "Member not found"

      app.stop
    }

    "Return BAD_REQUEST  and throw exception" in {
      when(mockMemberRespository.deleteMemberById(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, routes.MemberController.deleteMember(Card("testId")).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }
  }

  "increaseBalance" must {
    "return 'success if valid data is input" in {

      when(mockMemberRespository.increaseBalance(any, any))
        .thenReturn(Future.successful(Some(Members("testId", "test", "test", "test", 123, 123))))

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.MemberController.increaseBalance(Card("testId"), 234).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe 200
      contentAsString(result) mustBe "Document updated!"

      app.stop
    }

    "return correct error message if negative increase data is input" in {

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.MemberController.increaseBalance(Card("testId"), -234).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Minimum increase must be greater than zero"

      app.stop
    }

    "return correct error message and status if member not found" in {

      when(mockMemberRespository.getMemberById(Card("dftgyh")))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.MemberController.increaseBalance(Card("dftgyh"), 234).url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "No Member with that id exists in records"

      app.stop
    }
  }
  "decreaseBalance" must {
    "return 'success if valid data is input" in {

      when(mockMemberRespository.decreaseBalance(any, any))
        .thenReturn(Future.successful(Some(Members("testId", "test", "test", "test", 123, 123))))

      when(mockMemberRespository.getMemberById(any))
        .thenReturn(Future.successful(Some(Members("testId", "test", "test", "test", 123, 123))))


      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.MemberController.decreaseBalance(Card("testId"), 100).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe 200
      contentAsString(result) mustBe "Document updated!"

      app.stop
    }

    "return correct error message if decrease is higher than balance data is input" in {

      when(mockMemberRespository.getMemberById(any))
        .thenReturn(Future.successful(Some(Members("testId", "test", "test", "test", 123, 123))))

      when(mockMemberRespository.decreaseBalance(any, any))
        .thenReturn(Future.successful(Some(Members("testId", "test", "test", "test", 123, 123))))

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.MemberController.decreaseBalance(Card("testId"), 234).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Decrease cannot be greater than current balance"

      app.stop
    }

    "return correct error message and status if member not found" in {
      when(mockMemberRespository.getMemberById(any))
        .thenReturn(Future.successful(Some(Members("testId", "test", "test", "test", 123, 123))))

      when(mockMemberRespository.getMemberById(Card("dftgyh")))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.MemberController.decreaseBalance(Card("dftgyh"), 234).url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "No Member with that id exists in records"

      app.stop
    }
  }
  "updateMemberName" must {

    "return success and correct status" in {

      when(mockMemberRespository.updateName(any, any))
        .thenReturn(Future.successful(Some(Members("testId", "testName", "test", "test", 123, 123))))

      when(mockMemberRespository.getMemberById(any))
        .thenReturn(Future.successful(Some(Members("testId", "testName", "test", "test", 123, 123))))


      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.MemberController.updateMemberName(Card("testName"), "fred").url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe 200
      contentAsString(result) mustBe "Success! updated Member with id testId's name to fred"

      app.stop
    }
    "return correct error message if member does not exist in data" in {

      when(mockMemberRespository.updateName(any, any))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.MemberController.updateMemberName(Card("dftgyh"), "fred").url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "No Member with that id exists in records"
      status(result) mustBe NOT_FOUND


      app.stop
    }
    "return correct error message exception thrown" in {

      when(mockMemberRespository.updateName(any, any))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.MemberController.updateMemberName(Card("dftgyh"), "fred").url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }
  }
}
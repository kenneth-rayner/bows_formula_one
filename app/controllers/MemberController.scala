package controllers

import java.time.LocalDateTime

import controllers.repositories.{MemberRepository, SessionRepository}
import javax.inject.Inject
import models.{Card, Members, UserSession}
import play.api.Configuration
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc._
import reactivemongo.core.errors.DatabaseException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MemberController @Inject()(cc: ControllerComponents,
                                 config: Configuration,
                                 memberRepository: MemberRepository,
                                 sessionRepository: SessionRepository)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {


  def present(_id: Card) = Action.async {
    implicit request =>
      memberRepository.getMemberById(_id).flatMap {
        case Some(members) =>
          sessionRepository.getSession(_id).flatMap {
            case Some(_) =>
              sessionRepository.deleteSessionById(_id).map(_ => Ok(s"Goodbye ${members.name}"))
            case None =>
              sessionRepository.createNewSession(UserSession(_id._id, LocalDateTime.now))


                .map(_ => Ok(s"Hello ${members.name}"))

          }

        case None => Future.successful(BadRequest("Please register"))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Members model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  //GET
  def getMemberById(_id: Card): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      memberRepository.getMemberById(_id).map {
        case None => NotFound("Member not found")
        case Some(member) => Ok(Json.toJson(member))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Members model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  //GET
  def getBalance(_id: Card) = Action.async {
    implicit request: Request[AnyContent] =>
      memberRepository.getMemberById(_id).map {
        case Some(member) => Ok(Json.toJson(member.balance))
        case None => NotFound("Member not found!")
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Members model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def addNewMember: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      (for {
        member <- Future.fromTry(Try {
          request.body.as[Members]
        })
        result <- memberRepository.addNewMember(member)
      } yield Ok("Success")).recoverWith {
        case e: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Member model. Incorrect data!"))
        case e: DatabaseException =>
          Future.successful(BadRequest(s"Could not parse Json to Member model. Duplicate key error!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def deleteMember(_id: Card) = Action.async {
    implicit request =>
      memberRepository.deleteMemberById(_id).map {
        case Some(_) => Ok("Success")
        case _ => NotFound("Member not found")
      } recoverWith {
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  //POST
  def updateMemberName(_id: Card, newData: String): Action[AnyContent] = Action.async {
    implicit request =>
      memberRepository.updateName(_id, newData).map {

        case Some(member) =>
          Ok(s"Success! updated Member with id ${member._id._id}'s name to $newData")
        case _ =>
          NotFound("No Member with that id exists in records")
      } recoverWith {
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def increaseBalance(_id: Card, increase: Int): Action[AnyContent] = Action.async {

    memberRepository.getMemberById(_id).flatMap {
      case Some(_) =>
        increase match {
          case x if x <= 0 => Future.successful(BadRequest("Minimum increase must be greater than zero"))
          case _ =>
            memberRepository.getMemberById(_id).flatMap {
              case Some(_) => memberRepository.increaseBalance(_id, increase)
                .map { _ => Ok(s"Document updated!") }
            }
        }
      case None => Future.successful(NotFound("No Member with that id exists in records"))
    } recoverWith {
      case _ => Future.successful(BadRequest(s"Could not parse Json to Member model. Incorrect data!"))
      case e => Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))

    }
  }

  def decreaseBalance(_id: Card, decrease: Int): Action[AnyContent] = Action.async {
    memberRepository.getMemberById(_id).flatMap {
      case Some(members) => {
        decrease match {
          case x if x <= 0 => Future.successful(BadRequest("Minimum increase must be greater than zero"))
          case x if x > members.balance => Future.successful(BadRequest("Decrease cannot be greater than current balance"))
          case _ =>
            memberRepository.getMemberById(_id).flatMap {
              case Some(member) =>
                memberRepository.decreaseBalance(_id, decrease).map {
                  case Some(_) => Ok("Document updated!")
                  case None => NotFound("Member not found")
                }
            }
        }

      }
      case None => Future.successful(NotFound("No Member with that id exists in records"))

    }.recoverWith {
      case _ => Future.successful(BadRequest(s"Could not parse Json to Member model. Incorrect data!"))
      case e => Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
    }
  }
}





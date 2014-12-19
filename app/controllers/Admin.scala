package controllers

import actors.{MailActor, SendWelcome}
import akka.actor.Props
import models.{DBAchievementDao, Achievement, AccessManager}
import play.api.libs.concurrent.Akka
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import utils.SecuredController

/**
 * @author Emmanuel Nhan
 */
object Admin extends SecuredController{


  case class AllowedUser(code: String, email: String)


  val accessCode = "taine"

  implicit val reads: Reads[AllowedUser] = (
    (JsPath \ "code").read[String] and
      (JsPath \ "email").read[String](Reads.email)
    )(AllowedUser.apply _)

  implicit val writes: Writes[Achievement] = (
    (JsPath \ "id").writeNullable[Long] ~
      (JsPath \ "assignmentId").write[Long] ~
      (JsPath \ "fileName").write[String]
    )(unlift(Achievement.unapply))

  def grantAccess() = AdminSecuredAction(parse.json){ implicit request =>

    def createAccess(email: String) = {
      AccessManager.grantAccessTo(email)
      // Send email
      val mailActor = Akka.system.actorOf(Props[MailActor])
      mailActor ! SendWelcome(email)
    }


    request.body.validate[AllowedUser].fold( err => BadRequest, valid =>
      if (valid.code == accessCode) {
        createAccess(valid.email)
        Ok
      } else BadRequest
    )
  }

  def grantedAccess(state: String) = AdminSecuredAction{
    // TODO
    state match{
      case "all" => Ok(Json.toJson(AccessManager.allAccess))
      case "engaged" => Ok(Json.toJson(AccessManager.allAccess))
      case "not-engaged" => Ok(Json.toJson(AccessManager.allAccess))
      case _ => BadRequest
    }
  }

  def deleteAccess = AdminSecuredAction{
    AccessManager.deleteAll
    Ok
  }

  def remindNotEngaged() = Action{
    // TODO
    Ok(Json.obj("hits" -> 42))
  }

  def remindEngaged() = Action{
    // TODO
    Ok(Json.obj("hits" -> 32))
  }

  def uploadList() = AdminSecuredAction{
    val achievements = DBAchievementDao.getAll
    Ok(Json.toJson(achievements))
  }
}


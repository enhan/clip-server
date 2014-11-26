package controllers

import actors.{MailActor, SendWelcome}
import akka.actor.Props
import models.AccessManager
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

  def grantedAccess = AdminSecuredAction{
    Ok(Json.toJson(AccessManager.allAccess))
  }

}

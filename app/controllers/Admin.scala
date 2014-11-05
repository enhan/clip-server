package controllers

import models.AccessManager
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.MailSender
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * @author Emmanuel Nhan
 */
object Admin extends Controller{


  case class AllowedUser(code: String, email: String)


  val accessCode = "taine"

  implicit val reads: Reads[AllowedUser] = (
    (JsPath \ "code").read[String] and
      (JsPath \ "email").read[String](Reads.email)
    )(AllowedUser.apply _)


  def grantAccess() = Action.async(parse.json){ implicit request =>

    def createAccess(email: String) = {
      AccessManager.grantAccessTo(email)
      // Send email
      MailSender.sendWelcomeMail(email)
    }


    request.body.validate[AllowedUser].fold( err => Future.successful(BadRequest), valid =>
      if (valid.code == accessCode) createAccess(valid.email).map{r => Ok}  else Future.successful(BadRequest)
    )
  }

  def grantedAccess = Action{
    Ok(Json.toJson(AccessManager.allAccess))
  }

}

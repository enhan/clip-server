package controllers

import models.AccessManager
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * @author Emmanuel Nhan
 */
object Access extends Controller{

  case class AccessRequest(email: String, code: String)

  implicit val accessRequestReads: Reads[AccessRequest] = (
    (__ \ "email").read[String](Reads.email) and (__ \ "code").read[String]
    )(AccessRequest.apply _)

  val code = "lizbethdadou2015"

  def controlAccess() = Action(parse.json){ request =>
    request.body.validate[AccessRequest].fold(err => BadRequest, payload =>
      if (payload.code.equals(code) && AccessManager.hasAccess(payload.email))
        Ok
      else
        Forbidden
    )

  }

}

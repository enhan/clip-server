package controllers

import models._
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
/**
 * @author Emmanuel Nhan
 */
object Engagements extends Controller{

  val assignmentDao: AssignmentDao = DBAssignmentDao

  implicit val engagementsReads: Reads[Engagement] = (
    (JsPath \ "assignmentId").read[Long](Reads.filter[Long](ValidationError("assignment.not.found"))(l => DBAssignmentDao.findById(l).isDefined)) and
      (JsPath \ "email").read[String](Reads.email)
    )((assignment, email) => Engagement(None, email, assignment, completed = false))

  def createEngagements = Action(parse.json){ implicit request =>
    request.body.validate[List[Engagement]].map{engagements =>
      val created = engagements.map(DBEngagementDao.createEngagement)
      // TODO : SEND mail
      Ok
    }.getOrElse{
      BadRequest
    }

  }

}

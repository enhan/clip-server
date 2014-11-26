package controllers

import actors.{SendSummary, MailActor}
import akka.actor.Props
import models._
import play.api.data.validation.ValidationError
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Akka
import play.api.Play.current



/**
 * @author Emmanuel Nhan
 */
object Engagements extends Controller{

  val assignmentDao: AssignmentDao = DBAssignmentDao

  implicit val engagementsReads: Reads[Engagement] = (
    (JsPath \ "assignmentId").read[Long](Reads.filter[Long](ValidationError("assignment.not.found"))(l => DBAssignmentDao.findById(l).isDefined)) and
      (JsPath \ "email").read[String](Reads.email)
    )((assignment, email) => Engagement(None, email, assignment, completed = false))


  implicit val engagementsWrites: Writes[Engagement] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "email").write[String] and
      (JsPath \ "assignment").write[Long] and
      (JsPath \ "completed").write[Boolean]
    )(unlift(Engagement.unapply))

  def createEngagements = Action(parse.json){ implicit request =>
    request.body.validate[List[Engagement]].map{engagements =>
      val created = engagements.map(DBEngagementDao.createEngagement)
      // Create a map with songs
      val email = created.head.email
      def appendRecursive(toParse: List[Engagement], acc: Map[Song, List[Assignment]]): Map[Song,List[Assignment]] ={
        toParse match {
          case Nil => acc
          case head :: tail =>
            val assignment = DBAssignmentDao.findById(head.assignmentId).get
            val song = DBSongDao.findById(assignment.songId).get
            val newList = if (acc.contains(song)) assignment :: acc(song) else List(assignment)
            val newAcc = acc +(song -> newList)
            appendRecursive(tail, newAcc)
        }
      }

      val data = appendRecursive(created, Map[Song, List[Assignment]]())
      val mailActor = Akka.system.actorOf(Props[MailActor])
      mailActor ! SendSummary(email, data)
      Ok
    }.getOrElse{
      BadRequest
    }

  }

  def engagementsByMail(email:String) = Action{ implicit request=>
    val engagements =DBEngagementDao.findByEmail(email)
    Ok(Json.toJson(engagements))
  }

  def allEngagements = Action{ implicit request =>
    val engagements = DBEngagementDao.findAll
    Ok(Json.toJson(engagements))
  }

  def deleteAll() = Action{implicit request =>
    DBEngagementDao.clearAll
    Ok
  }
}

package controllers

import java.io.File
import java.util.UUID

import actors.{SendSummary, MailActor}
import akka.actor.Props
import models._
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import utils.SecuredController


/**
 * @author Emmanuel Nhan
 */
object Engagements extends SecuredController{

  val assignmentDao: AssignmentDao = DBAssignmentDao

  val fileBucket = play.api.Play.configuration.getString("upload.folder").getOrElse("/tmp/videos")

  implicit val engagementsReads: Reads[Engagement] = (
    (JsPath \ "assignmentId").read[Long](Reads.filter[Long](ValidationError("assignment.not.found"))(l => DBAssignmentDao.findById(l).isDefined)) and
      (JsPath \ "email").read[String](Reads.email)
    )((assignment, email) => Engagement(None, email, assignment, completed = false))


  implicit val engagementsWrites: Writes[Engagement] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "email").write[String] and
      (JsPath \ "assignment").write[Long] and
      (JsPath \ "completed").write[Boolean] and
      (JsPath \ "date").write[String]
    )(e => (e.id, e.email, e.assignmentId, e.completed, "2014-12-18"))

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

  def engagementsByMail(email:String) = AdminSecuredAction{ implicit request=>
    val engagements =DBEngagementDao.findByEmail(email)
    Ok(Json.toJson(engagements))
  }

  def allEngagements = AdminSecuredAction{ implicit request =>
    val engagements = DBEngagementDao.findAll
    Ok(Json.toJson(engagements))
  }

  def deleteAll() = AdminSecuredAction{implicit request =>
    DBEngagementDao.clearAll
    Ok
  }

  def myEngagements(songId: Long, by: String, state: String) = Action{
    state match{
      case "pending" => Ok(Json.toJson(DBEngagementDao.findPendingByEmailOnSong(by, songId)))
      case "completed" => Ok(Json.toJson(DBEngagementDao.findCompletedByEmailOnSong(by, songId)))
      case "all" => Ok(Json.toJson(DBEngagementDao.findByEmailOnSong(by, songId)))
      case _ => BadRequest
    }
  }

  def upload(id: Long) = Action(parse.multipartFormData) { request =>
    Logger.debug(s"Uploading video for engagement $id")
    request.body.file("video").map { videoFile =>
      import java.io.File
      DBEngagementDao.findById(id) match {
        case Some(e) =>
          val fileName = UUID.randomUUID().toString
          val achievement = Achievement(None, e.assignmentId, fileName)
          // Ensure fileBucket is up
          new File(fileBucket).mkdirs()
          videoFile.ref.moveTo(new File(fileBucket + "/" + fileName))
          DBEngagementDao.updateEngagement(e.copy(completed = true))
          DBAchievementDao.create(achievement)
          Ok.withHeaders(("Access-Control-Allow-Origin","*" ))
        case None =>
          Logger.debug(s"Engagement $id not found !")
          NotFound.withHeaders(("Access-Control-Allow-Origin","*" ))
      }
    }.getOrElse{
      BadRequest.withHeaders(("Access-Control-Allow-Origin","*" ))
    }


  }
}

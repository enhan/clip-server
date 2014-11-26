package controllers

import models._
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * @author Emmanuel Nhan
 */
object Assignments extends Controller{

  implicit val assignmentWrites: Writes[Assignment] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "pre").write[String] and
      (JsPath \ "post").write[String] and
      (JsPath \ "content").write[String] and
      (JsPath \ "available").write[Boolean] and
      (JsPath \ "startTime").write[String]
    )((a:Assignment) => (a.id, a.pre, a.post, a.content, a.spotsTaken < a.spots, a.startTime))

  implicit val songWrites: Writes[Song] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "videoLink").write[String] and
      (JsPath \ "assignments").write[List[Assignment]]
    )(unlift(Song.unapply))

  val songCreateReads: Reads[(String, String, String, String)] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "content").read[String] and
      (JsPath \ "code").read[String] and
      ((JsPath \ "videoLink").read[String] or Reads.pure(""))
    ).tupled

  val songUpdate: Reads[(String, String)] = (
      (JsPath \ "videoLink").read[String] and
      (JsPath \ "code").read[String]
    ).tupled

  val assignmentUpdate: Reads[(String, String)] = (
    (JsPath \ "startTime").read[String] and (JsPath \ "code").read[String]
    ).tupled

  val songDao: SongDao = DBSongDao

  val assignmentDao: AssignmentDao = DBAssignmentDao

  val acceptedCode = "taine"

  def getAll = Action{
    Ok(Json.toJson(songDao.findAllWithAssignments))
  }

  def createSong = Action(parse.json){ request =>
    Logger.debug("Create a song")
    request.body.validate(songCreateReads).fold(errors => BadRequest,
      t =>{
        // First validate code
        if (acceptedCode.equals(t._3)){
          val song = songDao.create(Song(None, t._1, t._4))
          val generator = new AssignmentGenerator(song.id.get, 3)
          generator.parseSong(t._2).foreach(assignmentDao.create)
          Ok
        }else{
          BadRequest(Json.obj("errors" -> "Code invalide"))
        }
      }
    )
  }

  def updateSong(id: Long) = Action(parse.json){ request =>
    songDao.findById(id).map{ song =>
      request.body.validate[(String, String)](songUpdate).fold(errors => BadRequest,
      payload => if (acceptedCode.equals(payload._2)){
        songDao.update(song.copy(videoLink = payload._1))
        Ok
      }else{
        Forbidden
      })
    }.getOrElse{
      NotFound
    }
  }

  def updateAssignment(id: Long) = Action(parse.json){ request =>
    assignmentDao.findById(id).map{ a =>
      request.body.validate[(String, String)](assignmentUpdate).fold(errors => BadRequest,
      payload => if (acceptedCode.equals(payload._2)){
        assignmentDao.update(a.copy(startTime = payload._1))
        Ok
      }else{
        Forbidden
      }
      )
    }.getOrElse{
      NotFound
    }
  }

  def getAllAssignments = Action{
    Ok(Json.toJson(assignmentDao.findAll))
  }


  def assignmentsForSong(id: Long) = Action{ request =>
    Ok(Json.toJson(assignmentDao.findForSongId(id)))
  }
}

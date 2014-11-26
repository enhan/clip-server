package models

import anorm._
import play.api.Logger
import play.api.db.DB
import anorm.SqlParser._
import play.api.Play.current

/**
 * @author Emmanuel Nhan
 */
case class Engagement(id: Option[Long], email: String, assignmentId: Long, completed: Boolean)


trait EngagementDao{

  def createEngagement(engagement: Engagement): Engagement

  def clearAll: Unit

  def findByEmail(email:String) : List[Engagement]

}




object DBEngagementDao extends EngagementDao{

  val parser ={
    get[Long]("id") ~
    get[String]("email") ~
    get[Long]("assignment_id") ~
    get[Boolean]("completed") map{
      case id~email~assignment~completed => Engagement(Some(id), email, assignment, completed)
    }
  }

  override def createEngagement(engagement: Engagement): Engagement = DB.withTransaction{ implicit connection =>
    val id = SQL"""insert into Engagement (email, assignment_id, completed) values (${engagement.email}, ${engagement.assignmentId}, ${engagement.completed})""".executeInsert()
    engagement.copy(id = id)
  }

  override def clearAll: Unit = DB.withConnection{ implicit connection =>
    SQL"""delete from Engagement""".execute()
  }

  override def findByEmail(email: String): List[Engagement] = DB.withConnection{implicit connection =>
    SQL"""select id, email, assignment_id, completed from Engagement where email = $email """.executeQuery().parse(parser *)
  }
}
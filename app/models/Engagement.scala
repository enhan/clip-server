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



}


object DBEngagementDao extends EngagementDao{
  override def createEngagement(engagement: Engagement): Engagement = DB.withTransaction{ implicit connection =>
    val id = SQL"""insert into Engagement (email, assignment_id, completed) values (${engagement.email}, ${engagement.assignmentId}, ${engagement.completed})""".executeInsert()
    engagement.copy(id = id)
  }

}
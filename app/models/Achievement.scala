package models

import anorm._
import play.api.Logger
import play.api.db.DB
import anorm.SqlParser._
import play.api.Play.current

/**
 *
 * achievements
 * @author Emmanuel Nhan
 */
case class Achievement(id: Option[Long], assignment: Long, fileId: String) {

}


trait AchievementDao{

  def create(a: Achievement): Achievement

}

object DBAchievementDao extends AchievementDao{
  override def create(a: Achievement) = DB.withConnection{implicit  connection  =>
    val id: Option[Long] = SQL"""insert into Achievement (assignmentId, fileId) values (${a.assignment}, ${a.fileId})""".executeInsert()
    a.copy(id= id)
  }
}
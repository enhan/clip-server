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

  def getAll: List[Achievement]

}

object DBAchievementDao extends AchievementDao{

  val parser = get[Long]("id") ~ get[Long]("assignment_id") ~ get[String]("file_path") map {
    case id~assignment~file => Achievement(Some(id), assignment, file)
  }

  override def create(a: Achievement) = DB.withConnection{implicit  connection  =>
    val id: Option[Long] = SQL"""insert into Achievement (assignment_id, file_path) values (${a.assignment}, ${a.fileId})""".executeInsert()
    a.copy(id= id)
  }

  override def getAll: List[Achievement] = DB.withConnection{ implicit connection =>
    SQL"""select id, assignment_id, file_path from Achievement""".as(parser *)
  }
}
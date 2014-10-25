package models

import anorm._
import play.api.db.DB
import anorm.SqlParser._
import play.api.Play.current

/**
 * @author Emmanuel Nhan
 */
case class Song(id: Option[Long], name: String, assignments: List[Assignment] = Nil)

trait SongDao{

  def findAll: List[Song]

  def create(song: Song): Song

  def findAllWithAssignments: List[Song]

}


object DBSongDao extends SongDao{

  val assignmentDao: AssignmentDao = DBAssignmentDao

  val parser = {
    get[Long]("id") ~ get[String]("name") map {
      case id ~ name => Song(Some(id), name)
    }
  }

  override def findAll: List[Song] = DB.withConnection{implicit c =>
    val selectAll = SQL("""select id, name from Song""")
    selectAll.as(parser *).toList
  }

  override def create(song: Song): Song = DB.withConnection{implicit c =>

   val id = SQL("insert into Song (name) values ({songName})").on("songName" -> song.name).executeInsert()
    Song(id, song.name)
  }

  override def findAllWithAssignments: List[Song] = {
    // This sucks but for now, using n+1 select technique...
    findAll.map { song =>
      Song(song.id, song.name, assignmentDao.findForSongId(song.id.get))
    }
  }
}

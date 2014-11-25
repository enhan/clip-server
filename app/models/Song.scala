package models

import anorm._
import play.api.db.DB
import anorm.SqlParser._
import play.api.Play.current

/**
 * @author Emmanuel Nhan
 */
case class Song(id: Option[Long], name: String, videoLink: String, assignments: List[Assignment] = Nil)

trait SongDao{

  def update(song: Song): Song

  def findById(id: Long): Option[Song]

  def findAll: List[Song]

  def create(song: Song): Song

  def findAllWithAssignments: List[Song]

}


object DBSongDao extends SongDao{

  val assignmentDao: AssignmentDao = DBAssignmentDao

  val parser = {
    get[Long]("id") ~ get[String]("name") ~ get[Option[String]]("video_link") map {
      case id ~ name ~ videoLink => Song(Some(id), name, videoLink.getOrElse(""))
    }
  }

  override def findAll: List[Song] = DB.withConnection{implicit c =>
    val selectAll = SQL("""select id, name, video_link from Song""")
    selectAll.as(parser *).toList
  }

  override def create(song: Song): Song = DB.withConnection{implicit c =>
    val id = SQL("insert into Song (name) values ({songName})").on("songName" -> song.name).executeInsert()
    song.copy(id = id)
  }

  override def findAllWithAssignments: List[Song] = {
    // This sucks but for now, using n+1 select technique...
    findAll.map { song =>
      Song(song.id, song.name, song.videoLink, assignmentDao.findForSongId(song.id.get))
    }
  }

  override def findById(id: Long): Option[Song] = DB.withConnection{ implicit c =>
    SQL"""select id, name, video_link from Song where id = $id""".as(parser.singleOpt).headOption
  }

  override def update(song: Song): Song = DB.withConnection{ implicit c =>
    SQL"""update Song set name=${song.name}, video_link=${song.videoLink} where id = ${song.id}""".executeUpdate()
    song
  }
}

package models

import anorm._
import play.api.Logger
import play.api.db.DB
import anorm.SqlParser._
import play.api.Play.current

/**
 * @author Emmanuel Nhan
 */
case class Assignment(id: Option[Long], songId: Long, rank: Int, content: String, pre: String, post:String, spots:Int, spotsTaken: Long = 0, startTime: String = "")

trait AssignmentDao{
  def create(a :Assignment): Assignment

  def findAll: List[Assignment]

  def findForSongId(songId: Long): List[Assignment]

  def findById(id: Long): Option[Assignment]

  def update(a: Assignment): Assignment

}



object DBAssignmentDao extends AssignmentDao{

  val assignmentParser = {
    get[Long]("id") ~
    get[Long]("song_id") ~
    get[Int]("rank") ~
    get[String]("content") ~
    get[String]("pre") ~
    get[String]("post") ~
    get[Int]("spots") ~
    get[Long]("spots_taken") ~
    get[Option[String]]("start_time")  map {
      case id~songId~rank~content~pre~post~spots~spotsTaken~startTime => Assignment(Some(id), songId, rank, content, pre, post, spots, spotsTaken, startTime.getOrElse(""))
    }
  }

  override def create(a: Assignment): Assignment = DB.withConnection{implicit c =>
    val id = SQL"""insert into Assignment(song_id, rank, content, pre, post, spots) values (${a.songId}, ${a.rank}, ${a.content}, ${a.pre}, ${a.post}, ${a.spots})""".executeInsert()
    a.copy(id = id)
  }

  override def findAll: List[Assignment] = DB.withConnection{implicit c =>
    val selectAll = SQL"""select a.id as id, a.song_id as song_id, a.rank as rank, a.content as content, a.pre as pre, a.post as post, a.spots as spots, count(e.id) as spots_taken, a.start_time as start_time from Assignment a left join  Engagement e on (a.id = e.assignment_id) group by a.song_id, a.rank, a.content, a.pre, a.post, a.spots, a.id, a.start_time"""
    val result =selectAll.as(assignmentParser *).toList
    Logger.debug("RES = " + result)
    result
  }


  override def findForSongId(songId: Long): List[Assignment] = DB.withConnection{implicit c =>
    SQL""" select a.id as id, a.song_id as song_id, a.rank as rank, a.content as content, a.pre as pre, a.post as post, a.spots as spots, count(e.id) as spots_taken, a.start_time as start_time  from Assignment a left join  Engagement e on (a.id = e.assignment_id) where a.song_id = $songId group by a.song_id, a.rank, a.content, a.pre, a.post, a.spots, a.id, a.start_time order by a.rank""".as(assignmentParser *).toList
  }

  override def findById(id: Long): Option[Assignment] = DB.withConnection{implicit c =>
    SQL""" select a.id as id, a.song_id as song_id, a.rank as rank, a.content as content, a.pre as pre, a.post as post, a.spots as spots, count(e.id) as spots_taken, a.start_time as start_time  from Assignment a left join  Engagement e on (a.id = e.assignment_id) where a.id = $id group by a.song_id, a.rank, a.content, a.pre, a.post, a.spots, a.id, a.start_time""".as(assignmentParser.singleOpt)
  }

  override def update(a: Assignment): Assignment = DB.withConnection{ implicit c =>
    SQL"""update Assignment set start_time = ${a.startTime} where id = ${a.id}""".executeUpdate()
    a
  }
}


class AssignmentGenerator(val songId:Long, val spots: Int, val blockSize:Int = 2){

  def parse(preProcessedContent : List[String], rankOffset: Int = 0): List[Assignment] = {

    def recursiveParse(toParse: List[String], rank: Int, count: Int, blockAcc: List[String], acc: List[Assignment]): List[Assignment] = {

      lazy val previousContent: String = acc match {
        case Nil =>  ""
        case prevHead :: prevTail =>
          val split = prevHead.content.split('\n')
          split(split.length - 1)
      }

      toParse match {
        case Nil => blockAcc match {
          case Nil => acc
          case head :: tail => Assignment(None, songId, rank, (head /: blockAcc.tail ){(toAcc, res) => res + "\n" + toAcc}, previousContent, "", spots) :: acc
        }
        case head :: tail => if (count+1 == blockSize){

          val nextPost = tail match{
            case next :: nextTail => next
            case Nil => ""
          }

          recursiveParse(tail, rank+1, 0, Nil, Assignment(None, songId, rank, (head /: blockAcc ){(toAcc, res) => res + "\n" + toAcc}, previousContent, nextPost, spots ) :: acc)
        } else {
          recursiveParse(tail, rank, count+1, head :: blockAcc, acc)
        }
      }
    }

    recursiveParse(preProcessedContent, rankOffset, 0, Nil, Nil)

  }

  def preProcess(content: String) = content.split("\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]").toList

  def songSplit(content: String) = content.split("(\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]){2,}").map(preProcess).toList

  def parseSong(content: String) = {
    val splited = songSplit(content)

    def recursiveParseSong(toParse: List[List[String]], rank: Int): List[List[Assignment]] = {
      toParse match {
        case Nil => Nil
        case head :: tail => {
          val parsed = parse(head, rank)
          val newRank = parsed match {
            case Nil => rank
            case nh :: nt => nh.rank + 1
          }
          parsed :: recursiveParseSong(tail, newRank)
        }
      }
    }

    recursiveParseSong(splited, 0).flatten

  }

}
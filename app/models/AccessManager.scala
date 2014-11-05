package models

import anorm._
import play.api.Logger
import play.api.db.DB
import anorm.SqlParser._
import play.api.Play.current

/**
 * @author Emmanuel Nhan
 */
object AccessManager {


  def grantAccessTo(email: String): Unit = DB.withConnection{implicit c =>
    SQL"""INSERT INTO AllowedEmail(email) values ($email)""".executeInsert()
  }

  def hasAccess(email: String): Boolean = DB.withConnection{implicit c =>
    val firstRow = SQL"""SELECT count(*) as c from AllowedEmail where email = $email""".apply().head
    val count = firstRow[Long]("c")
    count > 0
  }


  def allAccess : List[String] = DB.withConnection{implicit c =>
    SQL"""SELECT * from AllowedEmail"""().map(row => row[String]("email")).toList
  }

}

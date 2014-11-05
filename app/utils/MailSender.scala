package utils

import models.{Song, Assignment}
import play.api.libs.ws.{WSAuthScheme, WS}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * @author Emmanuel Nhan
 */
object MailSender {

  val mailjetApiKey = play.api.Play.configuration.getString("mailjet.key").getOrElse("invalid")
  val mailjetApiPrivate = play.api.Play.configuration.getString("mailjet.private").getOrElse("invalid")
  val mailjetSender = play.api.Play.configuration.getString("mailjet.sender").getOrElse("madominh@makitude.com")

  def sendWelcomeMail(recipient: String): Future[Boolean] ={
    val mailContent= views.html.welcome().body
    val data : Map[String, Seq[String]]= Map("from"-> Seq[String](mailjetSender),
      "to" -> Seq[String](recipient),
      "subject" -> Seq[String]("Top secret : Surprise pour le mariage d'Elizabeth et Damien"),
      "html" -> Seq[String](mailContent)
    )

    WS.url("https://api.mailjet.com/v3/send/message").
      withAuth(mailjetApiKey, mailjetApiPrivate, WSAuthScheme.BASIC).
      post(data).map{response =>

      play.api.Logger.debug(s"Send mail result : ${response.statusText}")
      true
    }
  }


  def sendRecapMail(recipient:String, contentData: Map[Song, List[Assignment]]) : Future[Boolean] = {
    val mailContent = views.html.recap(contentData).body
    val data : Map[String, Seq[String]]= Map("from"-> Seq[String](mailjetSender),
      "to" -> Seq[String](recipient),
      "subject" -> Seq[String]("Top secret: le récap de mes engagements"),
      "html" -> Seq[String](mailContent)
    )

    WS.url("https://api.mailjet.com/v3/send/message").
      withAuth(mailjetApiKey, mailjetApiPrivate, WSAuthScheme.BASIC).
      post(data).map{response =>

      play.api.Logger.debug(s"Send mail result : ${response.statusText}")
      true
    }
  }

}

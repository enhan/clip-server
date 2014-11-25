package actors

import akka.actor.Actor
import models.{Assignment, Song}
import play.api.libs.ws.{WSAuthScheme, WS}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class SendWelcome(recipient: String)
case class SendSummary(recipient: String, contentData: Map[Song, List[Assignment]])



/**
 * @author Emmanuel Nhan
 */
class MailActor extends Actor{

  val mailjetApiKey = play.api.Play.configuration.getString("mailjet.key").getOrElse("invalid")
  val mailjetApiPrivate = play.api.Play.configuration.getString("mailjet.private").getOrElse("invalid")
  val mailjetSender = play.api.Play.configuration.getString("mailjet.sender").getOrElse("madominh@makitude.com")

  override def receive: Receive = {
    case SendWelcome(who) => sendWelcomeMail(who)
    case SendSummary(who, what) => sendRecapMail(who, what)
  }


  def sendWelcomeMail(recipient: String) = {
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
    }
  }

  def sendRecapMail(recipient:String, contentData: Map[Song, List[Assignment]]) = {
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
    }
  }

}

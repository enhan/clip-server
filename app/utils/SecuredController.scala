package utils


import org.apache.commons.codec.binary.Base64
import play.api.Logger
import play.api.libs.Codecs
import play.api.mvc._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._


/**
 * @author Emmanuel Nhan
 */
trait SecuredController extends Controller {

  object AdminSecuredAction extends ActionBuilder[Request] {
    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      val authorization = request.headers.get("Authorization")
      authorization.map { header =>

        val decoded = new String(Base64.decodeBase64(header.substring(6)))
        val twoPartsArray = decoded.split(":")

        if (twoPartsArray.length != 2) {
          Future.successful(Forbidden)
        } else {
          val login = twoPartsArray(0)
          val password = twoPartsArray(1)
          if (login.equalsIgnoreCase("admin") && password.equalsIgnoreCase("lvsslpdlba")) {
            block(request)
          } else {
            Future.successful(Forbidden)
          }
        }


      }.getOrElse {
        Future.successful(Forbidden)
      }
    }
  }

}


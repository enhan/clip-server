package utils

import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future


/**
 * @author Emmanuel Nhan
 */
object CORSFilter extends Filter{

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {

    def cors(result: Result): Result = {
      result.withHeaders("Access-Control-Allow-Origin" -> "*",
        "Access-Control-Allow-Methods" -> "POST, GET, OPTIONS, PUT, DELETE",
        "Access-Control-Allow-Headers" -> "x-requested-with,content-type,Cache-Control,Pragma,Date"
      )
    }

    next(request).transform(res => cors(res), th => th)

  }
}

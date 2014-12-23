package controllers

import play.api.mvc._

/**
 * @author Emmanuel Nhan
 */
object OptionsController extends Controller{

  def returnsOk(all: String) = Action{
    Ok("").withHeaders("Access-Control-Allow-Origin" -> "*", "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS, PUT, DELETE",
      "Access-Control-Allow-Credentials" -> "true",
      "Access-Control-Allow-Headers" -> "Origin,Content-Type,Accept, X-Requested-With, Referer, User-Agent, Authorization"
    )
  }

}

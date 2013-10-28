package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

import play.api.templates.Html

object Admin extends Controller {
  import model._

  def index = Action {
    Ok(views.html.admin())
  }

  def requestSettings = Action {
    Ok(Json.toJson(retrieveAdminSettings()))
  }

  def saveSettings = Action(parse.json) { request =>
    request.body.validate[Admin](adminReads).map { case x =>
      Ok(Json.toJson(Result(success = true)))
    }.recoverTotal { e =>
      BadRequest(Json.toJson(Result(success = false)))
    }
  }

}
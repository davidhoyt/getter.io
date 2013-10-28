package controllers.v1

import scala.util.{Failure, Success}

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

import play.api.templates.Html
import play.api.data.validation.ValidationError

object Admin extends Controller {
  import model._
  import model.v1._

  def index = Action {
    Ok(views.html.admin())
  }

  def requestSettings = Action {
    retrieveAdminSettings() match {
      case Success(settings) =>
        Ok(Json.toJson(settings))
      case Failure(exc) =>
        BadRequest(Json.toJson(Result(success = false, message = s"Unable to retrieve settings: $exc")))
    }
  }

  def saveSettings = Action(parse.json) { request =>
    request.body.validate[Admin].map { case x =>
      if (x.contentDir.length <= 0) {
        BadRequest(Json.toJson(Result(success = false, message = s"Cannot have an empty content directory")))
      } else if ((x.git ne null) && x.git.repositories.exists(r => r.serverPrefix == "" || r.cloneURL == "")) {
        BadRequest(Json.toJson(Result(success = false, message = s"Missing the server prefix or clone URL")))
      } else {
        Global.saveAdminSettings(x)
        Ok(Json.toJson(Result(success = true)))
      }
    }.recoverTotal { e =>
      BadRequest(Json.toJson(Result(success = false, message = s"Invalid JSON payload")))
    }
  }

  def update = Action(parse.json) { request =>
    request.body.validate[Update].map { case x =>
      if (Global.updatePrefix(x.serverPrefix))
        Ok(Json.toJson(Result(success = true)))
      else
        BadRequest(Json.toJson(Result(success = false, message = s"Unable to locate the requested repository. Have you saved your changes?")))
    }.recoverTotal { e =>
      BadRequest(Json.toJson(Result(success = false, message = s"Invalid JSON payload")))
    }
  }

  def clear = Action(parse.json) { request =>
    request.body.validate[Clear].map { case x =>
      if (Global.clearPrefix(x.serverPrefix))
        Ok(Json.toJson(Result(success = true)))
      else
        BadRequest(Json.toJson(Result(success = false, message = s"Unable to locate the requested repository. Have you saved your changes?")))
    }.recoverTotal { e =>
      BadRequest(Json.toJson(Result(success = false, message = s"Invalid JSON payload")))
    }
  }

}
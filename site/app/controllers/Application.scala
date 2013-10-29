package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

import play.api.templates.Html
import java.io.File

object Application extends Controller {
  import model._

  def serveFile(file: String) = Action {
    //Attempt to locate the file and serve it.
    Global.findFileForResource(file) match {
      case Some(resource) =>
        Ok.sendFile(
          content = resource,
          inline = true
        )
        .withHeaders(
          CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
          PRAGMA -> "no-cache",
          EXPIRES -> "0"
        )
      case None =>
        if (file == "" || file == "/") {
          Ok(views.html.welcome())
        } else {
          NotFound
        }
    }
  }
}
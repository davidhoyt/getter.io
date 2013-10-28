package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

import play.api.templates.Html

object Application extends Controller {

  def index = Action {
    Ok(Html("HOME"))
  }

}
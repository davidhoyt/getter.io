package model

import scala.util.{Success, Try}

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

package object v1 {
  implicit val gitRepositoryReads = Json.reads[GitRepository]
  implicit val gitRepositoryWrites = Json.writes[GitRepository]

  implicit val gitReads = Json.reads[Git]
  implicit val gitWrites = Json.writes[Git]

  implicit val adminReads = Json.reads[Admin]
  implicit val adminWrites = Json.writes[Admin]

  implicit val clearReads = Json.reads[Clear]
  implicit val clearWrites = Json.writes[Clear]

  implicit val updateReads = Json.reads[Update]
  implicit val updateWrites = Json.writes[Update]

  implicit val resultReads = Json.reads[Result]
  implicit val resultWrites = Json.writes[Result]

  case class Admin(contentDir: String = "", git: Git = Git())
  case class Git(repositories: Seq[GitRepository] = Seq())
  case class GitRepository(serverPrefix: String, cloneURL: String)

  case class Clear(serverPrefix: String)
  case class Update(serverPrefix: String)

  case class Result(success: Boolean, message: String = "")

  def retrieveAdminSettings(): Try[Admin] = {
    Global.retrieveAdminSettings()
  }

}
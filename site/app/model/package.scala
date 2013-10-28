import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

package object model {
  implicit val adminReads = Json.reads[Admin]
  implicit val adminWrites = Json.writes[Admin]

  implicit val repositoryReads = Json.reads[Repository]
  implicit val repositoryWrites = Json.writes[Repository]

  implicit val resultReads = Json.reads[Result]
  implicit val resultWrites = Json.writes[Result]

  implicit val seqRepository: Reads[Seq[Repository]] = new Reads[Seq[Repository]] {
    def reads(json: JsValue): JsResult[Seq[Repository]] = {
      for {
        arr <- (json \ "repositories").validate[JsArray]
        a <- arr.value
        r <- a.validate[Repository]
      } yield r
    }
  }

  case class Admin(rootDirectory: String, repositories: Seq[Repository] = Seq())
  case class Repository(serverPrefix: String, cloneURL: String)

  case class Result(success: Boolean, message: String = "")

  def retrieveAdminSettings(): Admin = {
    Admin("/tmp/my/foo/")
  }

}
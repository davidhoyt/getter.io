package model

import play.api._
import play.api.libs.json._
import scala.util._
import scalax.io.{Resource, Codec}
import java.io.{File, PrintWriter}
import java.nio.file.{LinkOption, Paths, Path}
import scala.collection.mutable

//import scalax.io.JavaConverters._

object Global extends GlobalSettings {
  import model.v1._

  def defaultContentDir =
    Play.current.configuration.getString("content.dir").get

  def adminSettingsFile =
    Play.current.configuration.getString("admin.settings.file").get

  def retrieveAdminSettings(): Try[Admin] = {
    try {
      val f = new File(adminSettingsFile)
      if (f.exists()) {
        val contents = Resource.fromFile(f).byteArray
        Json.fromJson[Admin](Json.parse(contents)).map { case x =>
          Success(x)
        }.recoverTotal { err =>
          Failure(throw new IllegalArgumentException("Unable to parse admin settings"))
        }
      } else {
        Success(Admin(contentDir = defaultContentDir))
      }
    } catch {
      case t: Throwable =>
        Success(Admin(contentDir = defaultContentDir))
    }
  }

  def saveAdminSettings(admin: Admin): Boolean = {
    val original = retrieveAdminSettings().get

    if (original.contentDir != admin.contentDir) {
      deleteAll(new File(original.contentDir))
    }

    //Are there any missing git repositories?
    //If so, remove them.
    val diff = original.git.repositories.map(_.serverPrefix).diff(admin.git.repositories.map(_.serverPrefix))
    println(s"DIFF $diff")
    for(prefix <- diff) {
      deletePrefix(prefix)
    }


    val output = Json.prettyPrint(Json.toJson(admin))
    val f = new PrintWriter(new File(adminSettingsFile))
    try f.write(output)
    finally f.close()

    true
  }

  def deleteAll(parent: File): Boolean = {
    val s = mutable.Stack[File]()
    val q = mutable.Queue[File](parent)

    while(!q.isEmpty) {
      val d = q.dequeue()
      val children = d.listFiles()
      s.push(d)

      if (children != null && !children.isEmpty) {
        for (f <- children) {
          if (f.isDirectory)
            q.enqueue(f)
          else
          if (!f.delete())
            return false
        }
      }
    }

    for(f <- s)
      if (!f.delete())
        return false

    true
  }

  def rootGitRepositoryForPrefix(prefix: String): Option[File] = {
    //Find associated git repo.
    val admin = retrieveAdminSettings().get
    admin.git.repositories.find(_.serverPrefix == prefix).fold[Option[File]](None) { _ =>
      val path_to_repo = Paths.get(admin.contentDir, if ("/" == prefix) "ROOT" else "OTHER", prefix).toAbsolutePath.toFile
      if (!path_to_repo.exists())
        path_to_repo.mkdirs()

      println(path_to_repo)
      Some(path_to_repo)
    }
  }

  def updatePrefix(prefix: String): Boolean = {
    rootGitRepositoryForPrefix(prefix).fold(false) { path =>
      println(s"Updating $path")
      true
    }
  }

  def clearPrefix(prefix: String): Boolean = {
    rootGitRepositoryForPrefix(prefix).fold(false) { path =>
      println(s"Clearing $path")
      deleteAll(path)
    }
  }

  def deletePrefix(prefix: String): Boolean = {
    println(s"Attempting to delete $prefix")
    rootGitRepositoryForPrefix(prefix).fold(false) { path =>
      println(s"Deleting $path")
      deleteAll(path)
    }
  }
}
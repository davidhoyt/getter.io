package model

import scala.util._
import scala.sys.process._
import scala.collection.mutable
import scalax.io.{Resource, Codec}

import play.api._
import play.api.libs.json._

import java.io.{File, PrintWriter}
import java.nio.file.{LinkOption, Paths, Path}
import java.util.UUID

object Global extends GlobalSettings {
  import model.v1._

  def defaultContentDir =
    Play.current.configuration.getString("content.dir").get

  def defaultPathToGitExecutable =
    Play.current.configuration.getString("path.to.git").get

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
        Success(Admin(contentDir = defaultContentDir, git = Git(pathToExecutable = defaultPathToGitExecutable)))
      }
    } catch {
      case t: Throwable =>
        Success(Admin(contentDir = defaultContentDir, git = Git(pathToExecutable = defaultPathToGitExecutable)))
    }
  }

  def saveAdminSettings(admin: Admin): Admin = {
    val original = retrieveAdminSettings().get

    if (original.contentDir != admin.contentDir) {
      deleteAll(new File(original.contentDir))
    }

    //Are there any missing git repositories?
    //If so, remove them.
    val diff = original.git.repositories.map(_.id).diff(admin.git.repositories.map(_.id))
    for(id <- diff) {
      deleteGitRepository(id)
    }

    //Generate new IDs if necessary.
    val updated_repos =
      for (r <- admin.git.repositories) yield {
        val id =
          if ("" == r.id)
            UUID.randomUUID().toString
          else
            r.id
        val trimmed = r.serverPrefix.trim
        val cleaned0 =
          if (trimmed.startsWith("/") || trimmed.startsWith("\\"))
            trimmed
          else
            "/" + trimmed
        val cleaned = cleaned0.replaceAllLiterally("\\", "/")
        r.copy(serverPrefix = cleaned, id = id)
      }

    val updated_admin = admin.copy(git = admin.git.copy(repositories = updated_repos))

    val output = Json.prettyPrint(Json.toJson(updated_admin))
    val f = new PrintWriter(new File(adminSettingsFile))
    try f.write(output)
    finally f.close()

    updated_admin
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

  def findFileForResource(resource: String): Option[File] = {


    retrieveAdminSettings() match {
      case Success(settings) =>
        val cleaned0 = resource.trim

        //Check for the default resource and use that.
        val cleaned1 =
          if (cleaned0 == "" || cleaned0 == "/" && settings.routeForDefault != "")
            settings.routeForDefault
          else
            cleaned0
        val cleaned = cleaned1.replaceAllLiterally("\\", "/")
        val index_of_first_slash =
          cleaned.indexOf("/")
        val first_part =
          if (index_of_first_slash >= 0)
            "/" + cleaned.substring(0, index_of_first_slash)
          else
            "/"

        //Attempt to find something that has a matching prefix first.
        for {
          r <- settings.git.repositories if r.serverPrefix == first_part
          f = Paths.get(settings.contentDir, r.id, cleaned).toFile if f.exists() && f.isFile
        } return Some(f)

        //If that can't be found, look at all the others and find the first matching one.
        for {
          r <- settings.git.repositories if r.serverPrefix != first_part
          f = Paths.get(settings.contentDir, r.id, cleaned).toFile if f.exists() && f.isFile
        } return Some(f)

        //Unable to find a matching resource.
        None
      case Failure(_) =>
        None
    }
  }

  def rootGitRepositoryForID(id: String)(ifDoesntExist: (Admin, GitRepository, File) => Boolean = null): Option[(Admin, GitRepository, File)] = {
    //Find associated git repo.
    val admin = retrieveAdminSettings().get
    admin.git.repositories.find(_.id == id).fold[Option[(Admin, GitRepository, File)]](None) { repo =>
      val path_to_repo = Paths.get(admin.contentDir, id).toAbsolutePath.toFile
      if (!path_to_repo.exists()) {
        path_to_repo.mkdirs()
        if (ifDoesntExist ne null)
          if (!ifDoesntExist(admin, repo, path_to_repo)) {
            //If the callback doesn't succeed, then clear out the directory we just created.
            deleteAll(path_to_repo)
            return None
          }
      }
      Some((admin, repo, path_to_repo))
    }
  }

  def gitClone(settings: Admin, repo: GitRepository, path: File): Boolean = {
    import scala.sys.process._

    val git = Seq(settings.git.pathToExecutable, "clone", "--depth", "1", "--branch", repo.branch, repo.cloneURL, ".")
    val p = Process(git, path)

    Logger.info(s"Running $p in $path")

    val proc = p run ProcessLogger(Logger.info(_))
    proc.exitValue() == 0
  }

  def gitPull(settings: Admin, repo: GitRepository, path: File): Boolean = {
    import scala.sys.process._

    val git = Seq(settings.git.pathToExecutable, "pull", "--rebase", "--ff-only")
    val p = Process(git, path)

    Logger.info(s"Running $p in $path")

    val proc = p run ProcessLogger(Logger.info(_))
    proc.exitValue() == 0
  }

  def updateGitRepository(id: String): Boolean = {
    rootGitRepositoryForID(id)(gitClone).fold(false) { case (settings, repo, path) =>
      Logger.info(s"Updating $path")
      gitPull(settings, repo, path)
    }
  }

  def clearGitRepository(id: String): Boolean = {
    rootGitRepositoryForID(id)(null).fold(false) { case (settings, repo, path) =>
      Logger.info(s"Clearing $path")
      deleteAll(path)
    }
  }

  def deleteGitRepository(id: String): Boolean = {
    println(s"Attempting to delete $id")
    rootGitRepositoryForID(id)(null).fold(false) { case (settings, repo, path) =>
      Logger.info(s"Deleting $path")
      deleteAll(path)
    }
  }
}
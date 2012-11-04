package code.snippet

import java.util.Date
import code.gh.GhRepository
import code.gh.GhResource
import code.gh.parse
import code.lib.DependencyFactory
import dispatch.json.JsArray
import dispatch.json.JsObject
import dispatch.Http
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Box
import net.liftweb.http.S
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.IterableConst
import code.gh.GhFile
import code.gh.Auth
import net.liftweb.common.Full
import code.editor.Editor
import code.gh.GhUser
import code.gh.GitHub.GhAuthInfo
import net.liftweb.common.Empty
import code.gh.GitHub
import net.liftweb.common.Failure
import net.liftweb.json._

class GithubRepo {

  private val ghUser = {
    GhAuthInfo.is match {
      case Full(ghUser) => ghUser
      case Empty => S.redirectTo(GitHub.GH_SIGNIN_URL)
      case Failure(msg, _, _) =>
        S.error(msg)
        S.redirectTo(GitHub.GH_SIGNIN_URL)
    }
  }

  private val repoName = {
    S.param("repoName") match {
      case Full(repoName) => repoName
      case Empty =>
        S.error("You need to specify repository name")
        S.redirectTo(GitHub.GH_INDEX_URL)
      case Failure(msg, _, _) =>
        S.error(msg)
        S.redirectTo(GitHub.GH_INDEX_URL)
    }
  }
  private val path = {
    S.param("path") match {
      case Full(path) => path
      case Empty =>
        S.error("You need to specify path of file/folder")
        S.redirectTo(GitHub.GH_INDEX_URL)
      case Failure(msg, _, _) =>
        S.error(msg)
        S.redirectTo(GitHub.GH_INDEX_URL)
    }
  }

  private val pathList = {
    path match {
      case "" => Nil
      case p => p split "/" toList
    }
  }

  def repoNavBar = {
    println("repoName: " + repoName)
    println("repoName: " + pathList)
    val locations = repoName :: pathList
    println(pathList.length)
    println("locations: " + locations)
    println("length: " + locations.length)
    val links = for {
      i <- 1 to locations.length
      name = locations(i - 1)
      link = (locations take i mkString "/")
    } yield (name, link)
    "span" #> links.map((name_link_pair) => {
      			"a [href]" #> ("/" + GitHub.GH_INDEX_NAME + "/" + name_link_pair._2) &
      			"a *" #> name_link_pair._1
      	})
   }

  def render = {
    try {
      val dir = GhRepository.get_dir(ghUser.login, repoName, path, ghUser.access_token)
      dir match {
        case json: JsArray =>
          val jsonList = parse.jsonList(json)
          val resources = jsonList.map { jsonObj => GhRepository.resource_json2obj(jsonObj) }
            ".repo_item" #> resources.map((r: GhResource) =>
              ".repo_item [class]" #> ("repo_" + r._type) &
                ".repo_link *" #> r.name &
                ".repo_link [href]" #> ("/" + GitHub.GH_INDEX_NAME + "/" + repoName + "/" + r.path))
        case json: JsObject =>
          val jsonObj = parse.jsonObj(json)
          val file = GhRepository.resource_json2obj(jsonObj).asInstanceOf[GhFile]
          val content_base64 = file.content match {
            case Some(c) => c
            case None => ""
          }
          val content = new String(new sun.misc.BASE64Decoder().decodeBuffer(content_base64))
          "*" #>
          	<div>
          		{Editor.display(file, content, file.name)}
          		<div class="lift:NewPost">
              		Create New Post form will go here. Will be replaced by editor later on.
          		</div>
          	</div>
        case _ => throw new RuntimeException("Not supposed to happen: in Github.view_repo")
      }
    } catch {
      case e => "ul" #> (e.getMessage())
    }
  }
}
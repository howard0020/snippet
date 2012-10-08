package code
package snippet

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
import code.gh.Auth.GhAuthInfo
import code.gh.Auth
import net.liftweb.common.Full
import code.editor.Editor
import code.gh.GhUser

class Github {
  val GITHUB_LINK = "/github/"
    
  def signin = {
    "a [href]" #> Auth.sign_url(Full("http://localhost:8080"))
  }

  def repo_nav_bar = {
    val repoName = S.param("repoName").get
    val path = S.param("path").get split "/" toList
    val locations = repoName :: path
    val links =  for{
      i <- 1 to locations.length
      name = locations(i-1)
      link = (locations take i mkString "/")
    } yield (name, link)
    "a" #> links.map((name_link_pair) =>
    		"a *" #> name_link_pair._1 &
    		"a [href]" #> (GITHUB_LINK + name_link_pair._2))
  }
  	
  def repos = {
    val ghUser = GhAuthInfo.is match {
      case Full(ghUser) => ghUser
      case _ => throw new RuntimeException("User should have already logged in.")
    }
//    val hhandler3 = GhUser.get_emails(ghUser.access_token)
//    val r = Http(hhandler3)
    val handler = GhRepository.get_user_repos(ghUser.login, ghUser.access_token)
    val repos = Http(handler)
    ".repo_item" #> repos.map((r) =>
        ".repo_link *" #> r.name &
        ".repo_link [href]" #> (GITHUB_LINK + r.name))
  }

  def view_repo = {
    val authInfo = GhAuthInfo.is match {
      case Full(auth) => auth
      case _ => throw new RuntimeException("User should have already logged in.")
    }
    
    val repoName = S.param("repoName").get
    val path = S.param("path").get

    val handler = GhRepository.get_dir(authInfo.login, repoName, path, authInfo.access_token)
    println(handler.request.path.toString())
    
    try {
    val r = Http(handler)
    println("\n\nhello\n\n")
    r match {
      case json: JsArray =>
        val jsonList = parse.jsonList(json)
        val resources = jsonList.map { jsonObj => GhRepository.resource_json2obj(jsonObj) }
        "ul [id]" #> "repo_items" & 
        ".repo_item" #> resources.map((r: GhResource) =>
          ".repo_item [class]" #> ("repo_" + r._type) &
            ".repo_link *" #> r.name &
            ".repo_link [href]" #> (GITHUB_LINK + repoName + "/" + r.path))
      case json: JsObject =>
        val jsonObj = parse.jsonObj(json)
        val file = GhRepository.resource_json2obj(jsonObj).asInstanceOf[GhFile]
        val content_base64 = file.content match {
          case Some(c) => c
          case None => ""
        }
        val content = new String(new sun.misc.BASE64Decoder().decodeBuffer(content_base64))
        "ul" #> Editor.display(content, file.name)
      case _ => throw new RuntimeException("Not supposed to happen: in Github.view_repo")
    }
    } catch {
      case _ => "ul" #> "This repository is empty."
    }
    /*        val handler = GhRepository.get_dir(GithubAuth.name, repoName, path, GithubAuth.access_token)
    val json = Http(handler)

    try {
      val jsonList = parse.jsonList(json)
      val resources = jsonList.map { jsonObj => GhRepository.resource_json2obj(jsonObj) }
              "#repo *" #> resources.map((r: GhResource) =>
          ".repo_link *" #> r.name &
            ".repo_link [href]" #> ("/github/" + repoName + "/" + r.path))

    } catch {
      case _ =>
        val jsonObj = parse.jsonObj(json)
        GhRepository.resource_json2obj(jsonObj) match {
          case file: GhFile => "ul" #> <div>{file.content}</div>
          case _ => throw new RuntimeException("not supposed to happen")
        }
    }*/
  }
}


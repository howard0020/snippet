package code.gh

import dispatch._

import json._
import JsHttp._

import java.util.Date

case class GhOwner(id: Int, login: String)
case class GhRepository(id: Int, owner: GhOwner, name: String, updated_at: Date, language: String, html_url: String, clone_url: String, description: String, open_issues: Int)
abstract class GhResource { 
  val link:String
  val path:String
  val name:String
  val _type:String
}
case class GhDir(link: String, path: String, name: String) extends GhResource { val _type = "dir" }
case class GhFile(link: String, path: String, name: String, content: Option[String]) extends GhResource { val _type = "file" }

object GhRepository {
  val path_sep = "contents"

  val a = "https://api.github.com/repos/xiaoqiangwu2010/depot/contents/Rakefile"

  def get_dir(name: String, repoName: String, path: String, access_token: String) = {
    val svc = GitHub.api_host / "repos" / name / repoName / "contents" / path
    val handler = svc.secure <<? Map("access_token" -> access_token) ># { json => json }
    Http(handler)
  }
  def resource_json2obj(jsonObj: JsonObject): GhResource = {
    // sample link: 
    //       https://api.github.com/repos/xiaoqiangwu2010/depot/contents/Rakefile
    val link = jsonObj("_links").asObj("self").asString
    // need better way to rewrite
    val path = (link.substring("https://".length()) split "/" drop 5) mkString "/"
    val name = jsonObj("name").asString
    val _type = jsonObj("type").asString
    jsonObj("type").asString match {
      case "file" =>
        val content = if (jsonObj.contains("content")) Some(jsonObj("content").asString) else None
        GhFile(link, path, name, content)
      case "dir" =>
        GhDir(link, path, name)
      case _type => throw new RuntimeException("not suppose to have type: " + _type)
    }
  }
  def get_user_repos(user: String, access_token: String) = {
    val svc = GitHub.api_host / "users" / user / "repos"
    val handler = svc.secure <<? Map("access_token" -> access_token) ># { json =>
      val jsonList = parse.jsonList(json)

      jsonList.map { jsonObj =>

        val jsonOwnerObj = jsonObj("owner").asObj

        val id = jsonObj("id").asInt
        val owner_id = jsonOwnerObj("id").asInt
        val owner_login = jsonOwnerObj("login").asString
        val name = jsonObj("name").asString
        val updated_at = jsonObj("updated_at").asDate
        val language = jsonObj("language").asString
        val html_url = jsonObj("html_url").asString
        val clone_url = jsonObj("clone_url").asString
        val description = jsonObj("description").asString
        val open_issues = jsonObj("open_issues").asInt

        GhRepository(id, GhOwner(owner_id, owner_login), name, updated_at, language, html_url, clone_url, description, open_issues)
      }
    }
    Http(handler)
  }

  def get_org_repos(org_login: String, access_token: String) = {
    val svc = GitHub.api_host / "orgs" / org_login / "repos"
    svc.secure <<? Map("access_token" -> access_token) ># { json =>
      val jsonList = parse.jsonList(json)
      jsonList.map { jsonObj =>

        val jsonOwnerObj = jsonObj("owner").asObj

        val id = jsonObj("id").asInt
        val owner_id = jsonOwnerObj("id").asInt
        val owner_login = jsonOwnerObj("login").asString
        val name = jsonObj("name").asString
        val updated_at = jsonObj("updated_at").asDate
        val language = jsonObj("language").asString
        val html_url = jsonObj("html_url").asString
        val clone_url = jsonObj("clone_url").asString
        val description = jsonObj("description").asString
        val open_issues = jsonObj("open_issues").asInt

        GhRepository(id, GhOwner(owner_id, owner_login), name, updated_at, language, html_url, clone_url, description, open_issues)
      }
    }
  }
}
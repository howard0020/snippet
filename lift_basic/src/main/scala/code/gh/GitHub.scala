package code.gh

import dispatch._
import net.liftweb._
import common._
import http._
import sitemap._
import Loc._
import code.model._

object GitHub {
  val host = :/("github.com")
  val api_host = :/("api.github.com")

  def init {
    LiftRules.rewrite.append {
    case RewriteRequest(ParsePath(List("github", repoName, repo_path @ _*), suffix, _, _), _, _) =>
      val path = (repo_path mkString "/") + (suffix match {
        case "js" => ".js"
        case "css" => ".css"
        case _ => ""
      })
      RewriteResponse("github_repo" :: Nil, Map("repoName" -> repoName, "path" -> path))
    }
  }
  
  def sitemap: List[Menu] = {
    List[Menu](Menu.i("Github") / "github"
      >> If(
        () => User.loggedIn_? && Auth.is_logged_in,
        () => User.loggedIn_? match {
          case false => RedirectResponse(Auth.LOGIN_URL)
          case true => RedirectResponse(Auth.sign_url(Full("/github")))
        }),
      Menu(Loc("Github Repo", List("github_repo"), "Github Repo", Hidden,
        If(
          () => User.loggedIn_? && Auth.is_logged_in,
          () => User.loggedIn_? match {
            case false => RedirectResponse(Auth.LOGIN_URL)
            case true => RedirectResponse(
              Auth.sign_url(Full("/github/" + S.param("repoName").get +
                (S.param("path").get match {
                  case "" => ""
                  case path => "/" + path
                }))))
          }))),
      Menu(Loc("GhSignin", List("ghauth", "signin"), "GhSignin", Hidden)),
      Menu(Loc("GhCallback", List("ghauth", "callback"), "GhCallback", Hidden)),
      Menu(Loc("GhLoginUser", List("ghauth", "loginuser"), "GhLoginUser", Hidden)))
  }
}
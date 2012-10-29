package code.gh

import dispatch._
import net.liftweb._
import common._
import http._
import sitemap._
import Loc._
import code.model._
import code.share.SiteConsts

object GitHub {
  
  object GhAuthInfo extends SessionVar[Box[GhUser]](Empty)
  
  val host = :/("github.com")
  val api_host = :/("api.github.com")
  val NEXT_PAGE_NAME = "next"
    
  val GHLINK = "ghauth"
  val GH_SIGNIN_URL = SiteConsts.INDEX_URL  + "/" + GHLINK + "/" + SiteConsts.GH_SIGNIN_NAME
  val GH_CALLBACK_URL = SiteConsts.INDEX_URL  + "/" + GHLINK + "/" + SiteConsts.GH_CALLBACK_NAME
  val GH_LOGINUSER_URL = SiteConsts.INDEX_URL  + "/" + GHLINK + "/" + SiteConsts.GH_LOGINUSER_NAME

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
        () => User.loggedIn_? && GitHub.loggedIn_?,
        () => User.loggedIn_? match {
          case false => RedirectResponse(SiteConsts.LOGIN_URL)
          case true => RedirectResponse(GitHub.sign_url(Full("/github")))
        }),
      Menu(Loc("Github Repo", List("github_repo"), "Github Repo", Hidden,
        If(
          () => User.loggedIn_? && GitHub.loggedIn_?,
          () => User.loggedIn_? match {
            case false => RedirectResponse(SiteConsts.LOGIN_URL)
            case true => RedirectResponse(
              GitHub.sign_url(Full("/github/" + S.param("repoName").get +
                (S.param("path").get match {
                  case "" => ""
                  case path => "/" + path
                }))))
          }))),
      Menu(Loc("GhSignin", List(GHLINK, SiteConsts.GH_SIGNIN_NAME), "GhSignin", Hidden)),
      Menu(Loc("GhCallback", List(GHLINK, SiteConsts.GH_CALLBACK_NAME), "GhCallback", Hidden)),
      Menu(Loc("GhLoginUser", List(GHLINK, SiteConsts.GH_LOGINUSER_NAME), "GhLoginUser", Hidden)))
  }
  
    private def next: String = {
    S.param(NEXT_PAGE_NAME) openOr SiteConsts.GH_SUCCESS_URL
  }

  def sign_url(success_url: Box[String]) = {
    GitHub.GH_SIGNIN_URL + (success_url match {
      case Full(url) => "?" + NEXT_PAGE_NAME + "=" + url
      case _ => success_url
    })
  }
  def login_url = {
    sign_url(Full(GitHub.GH_LOGINUSER_URL))
  }

  def loggedIn_? = {
    GhAuthInfo.is match {
      case Full(auth) => true
      case _ => false
    }
  }

  def redirect_to_signin = {
    S.redirectTo(sign_url(S.param(NEXT_PAGE_NAME)))
  }

  def signin = {
    var req = Auth.authorize_uri(SiteConsts.GH_KEY, GitHub.GH_CALLBACK_URL + "?" + GitHub.NEXT_PAGE_NAME + "=" + next)
    S.redirectTo(req.to_uri.toString())
  }

  def callback = {
    val ghCode = S.param("code") openOr GitHub.redirect_to_signin
    var token = Auth.access_token(SiteConsts.GH_KEY, GitHub.GH_CALLBACK_URL, SiteConsts.GH_SECRET, ghCode)
    val user = GhUser.get_authenticated_user(token)
    GhAuthInfo(Full(user))
    S.redirectTo(next)
  }
}
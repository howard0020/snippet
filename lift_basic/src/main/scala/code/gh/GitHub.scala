package code.gh

import dispatch._
import net.liftweb._
import common._
import http._
import sitemap._
import Loc._
import code.model._
import code.share.SiteConsts

class GitHub(key: String, secret: String)

object GitHub {

  object GhAuthInfo extends SessionVar[Box[GhUser]](Empty)

  val host = :/("github.com")
  val api_host = :/("api.github.com")
  val NEXT_PAGE_NAME = "next"

  val GH_INDEX_NAME = "github"
  val GH_INDEX_URL = SiteConsts.INDEX_URL + "/" + GH_INDEX_NAME
  val GH_DISPATCH = "ghauth"
  val GH_SIGNIN_NAME = "signin"
  val GH_CALLBACK_NAME = "callback"

  val GH_SIGNIN_URL = SiteConsts.INDEX_URL + "/" + GH_DISPATCH + "/" + GH_SIGNIN_NAME
  val GH_CALLBACK_URL = SiteConsts.INDEX_URL + "/" + GH_DISPATCH + "/" + GH_CALLBACK_NAME

  def init {
    LiftRules.rewrite.append {
      case RewriteRequest(ParsePath(List(GH_INDEX_NAME, repoName, repo_path @ _*), suffix, _, _), _, _) =>
        val path = (repo_path mkString "/") + (suffix.isEmpty() match {
          case true => ""
          case false => "." + suffix
        })
        println("rewriting: " + repoName + ",  " + path)
        RewriteResponse("github_repo" :: Nil, Map("repoName" -> repoName, "path" -> path))
    }
  }

  def sitemap: List[Menu] = {
    List[Menu](Menu.i("Github") / GH_INDEX_NAME
      >> If(
        () => User.loggedIn_? && GitHub.signedIn_?,
        () => User.loggedIn_? match {
          case false => RedirectResponse(SiteConsts.LOGIN_URL)
          case true => RedirectResponse(GitHub.GH_SIGNIN_URL)
        }),
      Menu(Loc("Github Repo", List("github_repo"), "Github Repo", Hidden,
        If(
          () => User.loggedIn_? && GitHub.signedIn_?,
          () => User.loggedIn_? match {
            case false => RedirectResponse(SiteConsts.LOGIN_URL)
            case true => RedirectResponse(GitHub.GH_SIGNIN_URL)
          }))),
      Menu(Loc("GhSignin", List(GH_DISPATCH, GH_SIGNIN_NAME), "GhSignin", Hidden,
        If(
          () => User.loggedIn_? && !GitHub.signedIn_?,
          () =>
            User.loggedIn_? match {
              case false => RedirectResponse(SiteConsts.LOGIN_URL)
              case true => RedirectResponse(GH_INDEX_URL)
            }))),
      Menu(Loc("GhCallback", List(GH_DISPATCH, GH_CALLBACK_NAME), "GhCallback", Hidden,
        If(
          () => User.loggedIn_? && !GitHub.signedIn_?,
          () =>
            User.loggedIn_? match {
              case false => RedirectResponse(SiteConsts.LOGIN_URL)
              case true => RedirectResponse(GH_INDEX_URL)
            }))))
  }

  private def next: String = {
    S.param(NEXT_PAGE_NAME) openOr GH_INDEX_URL
  }

  def signinUrl(redirectUrl: Option[String] = None) = {
    val redirectLink = redirectUrl match {
      case None => GH_INDEX_URL
      case Some(url) => url
    }
    GitHub.GH_SIGNIN_URL + "?" + NEXT_PAGE_NAME + "=" + redirectLink
  }

  def signedIn_? = {
    GhAuthInfo.is match {
      case Full(auth) => true
      case _ => false
    }
  }

  def signin = {
    var req = Auth.authorize_uri(SiteConsts.GH_KEY, GitHub.GH_CALLBACK_URL + "?" + GitHub.NEXT_PAGE_NAME + "=" + next)
    S.redirectTo(req.to_uri.toString())
  }

  def callback = {
    try {
      val ghCode = S.param("code").get
      var token = Auth.access_token(SiteConsts.GH_KEY, GitHub.GH_CALLBACK_URL, SiteConsts.GH_SECRET, ghCode)
      val user = GhUser.get_authenticated_user(token)
      GhAuthInfo(Full(user))
      S.redirectTo(next)
    } catch {
      // invalid code or code is not passed
      case _ => S.redirectTo(GitHub.GH_SIGNIN_URL + "?" + NEXT_PAGE_NAME + "=" + next)
    }
  }
}
package code.gh

import dispatch.oauth.OAuth._
import dispatch._
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SessionVar
import net.liftweb.util.Props
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import code.share.SiteConsts

object Auth {

  object GhAuthInfo extends SessionVar[Box[GhUser]](Empty)

  val NEXT_PAGE_NAME = "next"
  val GH_LOGIN_REDIRECT = "http://localhost:8080/ghauth/loginuser"
  val svc = GitHub.host / "login" / "oauth"

  def authorize_uri(client_id: String, redirect_uri: String) = {
    svc.secure / "authorize" <<? Map("client_id" -> client_id, "redirect_uri" -> redirect_uri)
  }
  def authorize_uri(client_id: String, redirect_uri: String, scope: List[Scope.Value]) =
    svc.secure / "authorize" <<? Map("client_id" -> client_id, "redirect_uri" -> redirect_uri, "scope" -> scope.mkString(","))

  def access_token(client_id: String, redirect_uri: String, client_secret: String, code: String) = {
    val handler = svc.secure.POST / "access_token" <<?
      Map("client_id" -> client_id,
        "redirect_uri" -> redirect_uri,
        "client_secret" -> client_secret,
        "code" -> code) >% {
          m => m("access_token")
        }
    Http(handler)
  }

  private def next: String = {
    S.param(NEXT_PAGE_NAME) openOr SiteConsts.GH_SUCCESS_URL
  }

  def sign_url(success_url: Box[String]) = {
    SiteConsts.GH_SIGNIN_URL + (success_url match {
      case Full(url) => "?" + NEXT_PAGE_NAME + "=" + url
      case _ => success_url
    })
  }
  def login_url = {
    sign_url(Full(GH_LOGIN_REDIRECT))
  }

  def is_logged_in = {
    GhAuthInfo.is match {
      case Full(auth) => true
      case _ => false
    }
  }

  def redirect_to_signin = {
    S.redirectTo(sign_url(S.param(NEXT_PAGE_NAME)))
  }

  def signin = {
    var req = authorize_uri(SiteConsts.GH_KEY, SiteConsts.GH_CALLBACK_URL + "?" + Auth.NEXT_PAGE_NAME + "=" + next)
    S.redirectTo(req.to_uri.toString())
  }

  def callback = {
    val ghCode = S.param("code") openOr Auth.redirect_to_signin
    var token = access_token(SiteConsts.GH_KEY, SiteConsts.GH_CALLBACK_URL, SiteConsts.GH_SECRET, ghCode)
    val user = GhUser.get_authenticated_user(token)
    GhAuthInfo(Full(user))
    S.redirectTo(next)
  }
}

object Scope extends Enumeration {
  type Scope = Value
  val user, public_repo, repo, gist = Value
}

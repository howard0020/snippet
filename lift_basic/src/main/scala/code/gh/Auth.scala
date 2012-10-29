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

object Auth {

  object GhAuthInfo extends SessionVar[Box[GhUser]](Empty)

  val KEY_NAME = "gh.key"
  val SECRET_NAME = "gh.secret"
  val DEFAULT_SUCCESS_URL_NAME = "gh.success_url"
  val BASE_URL = "gh.base_url"
  val SIGNIN_URL_NAME = "gh.signin_url"
  val CALLBACK_URL = "gh.callback_url"
  val FAILURE_URL_NAME = "gh.failure_url"
  val NEXT_PAGE_NAME = "next"

  val key = Props.get(KEY_NAME) match { case Full(v) => v; case _ => throw new RuntimeException("Key is not set") }
  val secret = Props.get(SECRET_NAME) match { case Full(v) => v; case _ => throw new RuntimeException("Secret is not set") }
  val success_url = Props.get(DEFAULT_SUCCESS_URL_NAME) match { case Full(v) => v; case _ => throw new RuntimeException("Success URL is not set") }
  val base_url = Props.get(BASE_URL) match { case Full(v) => v; case _ => throw new RuntimeException("Base URL is not set") }
  val callback_url = Props.get(CALLBACK_URL) match { case Full(v) => v; case _ => throw new RuntimeException("Callback URL is not set") }
  val signin_url = Props.get(SIGNIN_URL_NAME) match { case Full(v) => v; case _ => throw new RuntimeException("Signin URL is not set") }
  val failure_url = Props.get(FAILURE_URL_NAME) match { case Full(v) => v; case _ => throw new RuntimeException("Failure URL is not set") }
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
    S.param(NEXT_PAGE_NAME) openOr success_url
  }

  def sign_url(success_url: Box[String]) = {
    signin_url + (success_url match {
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
    var req = authorize_uri(key, callback_url + "?" + Auth.NEXT_PAGE_NAME + "=" + next)
    S.redirectTo(req.to_uri.toString())
  }

  def callback = {
    val ghCode = S.param("code") openOr Auth.redirect_to_signin
    var token = access_token(key, callback_url, secret, ghCode)
    val user = GhUser.get_authenticated_user(token)
    GhAuthInfo(Full(user))
    S.redirectTo(next)
  }
}

object Scope extends Enumeration {
  type Scope = Value
  val user, public_repo, repo, gist = Value
}

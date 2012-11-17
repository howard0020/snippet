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
}

object Scope extends Enumeration {
  type Scope = Value
  val user, public_repo, repo, gist = Value
}

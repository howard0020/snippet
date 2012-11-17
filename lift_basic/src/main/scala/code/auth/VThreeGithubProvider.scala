package code.auth

import scala.xml.NodeSeq
import dispatch._
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.json.JsonParser
import omniauth.lib.GithubProvider
import omniauth.AuthInfo
import omniauth.Omniauth
import net.liftweb.http.SessionVar

class VThreeGithubProvider(clientId: String, secret: String) extends GithubProvider(clientId, secret) {
  private def createAPIRequest(accessToken: String): Request = {
    :/("api.github.com").secure / "user" <<? Map("access_token" -> accessToken)
  }

  
  override def validateToken(accessToken: String): Boolean = {
    val tempRequest = createAPIRequest(accessToken)
    try {
      val json = Omniauth.http(tempRequest >- JsonParser.parse)

      val uid = (json \ "id").extract[String]
      val name = (json \ "login").extract[String]

      val ai = AuthInfo(providerName, uid, name, accessToken)
      Omniauth.setAuthInfo(ai)
      GithubAuthInfo(Full(ai))
      logger.debug(ai)

      true
    } catch {
      case _ => false
    }
  }

  override def tokenToId(accessToken: String): Box[String] = {
    val tempRequest = createAPIRequest(accessToken)
    try {
      val json = Omniauth.http(tempRequest >- JsonParser.parse)
      Full((json \ "user" \ "id").extract[String])
    } catch {
      case _ => Empty
    }
  }
}

object GithubAuthInfo extends SessionVar[Box[AuthInfo]](Empty)

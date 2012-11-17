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
import code.gh.GitHub.GhAuthInfo
import code.gh.GhUser
import code.gh.GitHub
import code.share.SiteConsts
import net.liftweb.common.Failure
import code.model.User

class GhProvider(clientId: String, secret: String) extends GithubProvider(clientId, secret) {

  private def createAPIRequest(accessToken: String): Request = {
    :/("api.github.com").secure / "user" <<? Map("access_token" -> accessToken)
  }

  override def validateToken(accessToken: String): Boolean = {
    val tempRequest = createAPIRequest(accessToken)
    try {
      val json = Omniauth.http(tempRequest >- JsonParser.parse)

      val uid = (json \ "id").extract[String]
      val name = (json \ "login").extract[String]

      val avatar_url = (json \ "avatar_url").extract[String]
      val account_type = (json \ "avatar_url").extract[String]

      val user = GhUser(uid.toInt, name, avatar_url, account_type, accessToken)

      val ai = AuthInfo(providerName, uid, name, accessToken)
      Omniauth.setAuthInfo(ai)
      loginUser(user)
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

  def loginUser(ghUser: GhUser) = {
    GhAuthInfo(Full(ghUser))
    val email = "xiaoqiangwu2007@gmail.com"
    User.findUser(email) match {
      case Full(oldUser) =>
        User.logUserIn(oldUser)
      case Empty =>
        val newUser = User.create
        newUser.email.set(email)
        newUser.username.set(ghUser.login)
        newUser.iconURL.set(ghUser.avatar_url)
        newUser.save
        User.logUserIn(newUser)
      case Failure(msg, _, _) =>
        S.error(msg)
        GitHub.GhAuthInfo(Empty)
    }
  }

}

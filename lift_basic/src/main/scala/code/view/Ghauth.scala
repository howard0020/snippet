package code.view
import scala.xml.NodeSeq

import code.gh.Auth
import code.model.User
import net.liftweb.common.Full
import net.liftweb.http.LiftView
import net.liftweb.http.S

class Ghauth extends LiftView {
  
  override def dispatch = {
    case "signin" => signin _
    case "callback" => callback _
    case "loginuser" => loginuser _
  }

  def signin: NodeSeq = {
    Auth.signin
  }

  def callback: NodeSeq = {
    Auth.callback
  }
  
  def loginuser: NodeSeq = {
    Auth.GhAuthInfo.is match {
      case Full(ghUser) =>     
        val newUser = User.create
        val email = "xiaoqiangwu2007@gmail.com"
        newUser.email.set(email)
        newUser.username.set(ghUser.login)
        newUser.iconURL.set(ghUser.avatar_url)
        newUser.save
        User.logUserIn(newUser)
        S.redirectTo(Auth.HOME_URL)
      case _ => S.redirectTo(Auth.LOGIN_URL)
    }
  }
}






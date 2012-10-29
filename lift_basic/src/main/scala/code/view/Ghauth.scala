package code.view
import scala.xml.NodeSeq
import code.gh.Auth
import code.model.User
import net.liftweb.common.Full
import net.liftweb.http.LiftView
import net.liftweb.http.S
import code.share.SiteConsts
import code.gh.GitHub

class Ghauth extends LiftView {
  
  override def dispatch = {
    case SiteConsts.GH_SIGNIN_NAME => signin _
    case SiteConsts.GH_CALLBACK_NAME => callback _
    case SiteConsts.GH_LOGINUSER_NAME => loginuser _
  }

  def signin: NodeSeq = {
    GitHub.signin
  }

  def callback: NodeSeq = {
    GitHub.callback
  }
  
  def loginuser: NodeSeq = {
    GitHub.GhAuthInfo.is match {
      case Full(ghUser) =>     
        val newUser = User.create
        val email = "xiaoqiangwu2007@gmail.com"
        newUser.email.set(email)
        newUser.username.set(ghUser.login)
        newUser.iconURL.set(ghUser.avatar_url)
        newUser.save
        User.logUserIn(newUser)
        S.redirectTo(SiteConsts.INDEX_URL)
      case _ => S.redirectTo(SiteConsts.LOGIN_URL)
    }
  }
}






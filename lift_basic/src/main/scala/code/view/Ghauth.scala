package code.view
import scala.xml.NodeSeq
import code.gh.Auth
import code.model.User
import net.liftweb.common.Full
import net.liftweb.http.LiftView
import net.liftweb.http.S
import code.share.SiteConsts
import code.gh.GitHub
import net.liftweb.common.Failure
import net.liftweb.common.Empty

class Ghauth extends LiftView {

  override def dispatch = {
    case GitHub.GH_SIGNIN_NAME => signin _
    case GitHub.GH_CALLBACK_NAME => callback _
  }
  
  def signin: NodeSeq = {
    GitHub.signin
  }

  def callback: NodeSeq = {
    GitHub.callback
  }
}






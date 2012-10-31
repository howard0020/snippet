package code.comet

import scala.xml._
import code.model.CodeSnippet
import net.liftweb.actor.LiftActor
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.util._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import JsCmds._
import scala.xml.NodeSeq
import Helpers._
import code.model.Tag
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.mapper.By
import code.snippet.Post
import net.liftweb.common.Failure
import net.liftweb.common.Box
import code.model.SnippetTags
import code.model.User
import code.model.post.Block
import scala.xml.Attribute
import scala.Null
import code.search.SearchQuery
import code.search.SearchEngine
import code.share.PostSnippet
import code.share.SiteConsts

class ProfilePostActor extends CometActor {

  private def posts = {
    User.loggedIn_? match {
      case false => S.redirectTo(SiteConsts.LOGIN_URL)
      case true => User.currentUser.get.toc.posts
    }
  }

  def render = {
    PostSnippet.render(posts)
  }

  override def lowPriority = {
    case _ =>
      reRender(false)
  }
}
package code.comet

import scala.xml._
import code.model.PostModel
import net.liftweb.actor.LiftActor
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.util._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import JsCmds._
import scala.xml.NodeSeq
import Helpers._
import code.model.TagModel
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.mapper.By
import net.liftweb.common.Failure
import net.liftweb.common.Box
import code.model.SnippetTags
import code.model.UserModel
import code.model.post.BlockModel
import scala.xml.Attribute
import scala.Null
import code.search.SearchQuery
import code.search.SearchEngine
import code.share.PostSnippet
import code.share.SiteConsts

class ProfilePostActor extends CometActor {

  private def posts = {
    UserModel.loggedIn_? match {
      case false => S.redirectTo(SiteConsts.LOGIN_URL)
      case true => 
        println("comet posts")
        UserModel.currentUser.get.toc.tree.posts
    }
  }

  def render = {
    PostSnippet.render(posts)
  }

  override def lowPriority = {
    case _ =>
      println("comet rerendering posts")
      reRender(true)
  }
}
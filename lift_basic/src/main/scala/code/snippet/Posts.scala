package code.snippet

import net.liftweb.util.BindPlus._
import net.liftweb.util._
import code.model.User
import Helpers._
import net.liftweb.common.{Full, Empty, Box}
import code.model.CodeSnippet
import net.liftweb.common.Failure
import net.liftweb.http.SessionVar
import net.liftweb.http.S
import scala.xml.NodeSeq
import code.model._
import net.liftweb.mapper.By

class Posts {
  
	def setparam(tempalte: NodeSeq) = {
	  
	SnippetTags.getTopTag(1).foreach(tag => Console.println("===tagName>"+tag.name))
    val tag = S.param("tag")
    tag match {
      case Full(name) => Console.println("=Snippet=====> tag:" + name)
      case Empty => Console.println("=Snippet======> tag:Empty")
      case Failure(msg,_,_) => Console.println("=Snippet======> tag:Empty:Failure"+msg)
    }
    val tagStr = tag.openOr("")
    val name = S.attr("name",_.toString)
 	val tagObj = Tag.find(By(Tag.name,tagStr))
 	Console.println("===attr=>"+name.openOr(""))
 	S.session match {
      case Full(session) => session.setupComet("PostActor",name,tagObj)
      case Empty => S.error("no session!")
      case Failure(msg,_,_) => S.error("no session!"+msg)
    }
    tempalte
  }
}
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
import code.model.Tag
import net.liftweb.mapper.By

class Posts {
  
	def setparam(tempalte: NodeSeq) = {
    val tag = S.param("tag")
    tag match {
      case Full(name) => Console.println("=Snippet=====> tag:" + name)
      case Empty => Console.println("=Snippet======> tag:Empty")
      case Failure(msg,_,_) => Console.println("=Snippet======> tag:Empty:Failure"+msg)
    }
    for{
      session <- S.session
      tagStr <- S.param("tag")
    }{
      val name = S.attr("namehehe",_.toString)
      val tag = Tag.find(By(Tag.name,tagStr))
      Console.println("===attr=>"+name.openOr(""))
      session.setupComet("PostActor",name,tag)
    }
    tempalte
  }
}
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
  object tagVar extends SessionVar[Box[String]](Empty)
  
	def SetParam(tempalte: NodeSeq):NodeSeq = {
    val tag = S.param("tag")
    tag match {
      case Full(name) => Console.println("=1======> tag:" + name)
      case Empty => {}
    }
    tagVar.set(tag)
    
   // 
    tempalte
  }
}
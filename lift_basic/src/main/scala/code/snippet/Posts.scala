package code.snippet

import net.liftweb.util.BindPlus._
import net.liftweb.util._

import code.model.UserModel
import Helpers._
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.common.Failure
import net.liftweb.http.SessionVar
import net.liftweb.http.S
import scala.xml.NodeSeq
import code.model._
import net.liftweb.mapper.By
import net.liftweb.http.SHtml
import scala.xml.Text
import net.liftweb.http.js._
import net.liftweb.http.js.{JE,JsCmd,JsCmds}
import JsCmds._
import net.liftweb.http.js.JE.{JsRaw,Str}

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
 	val tagObj = TagModel.find(By(TagModel.name,tagStr))
 	Console.println("===attr=>"+name.openOr(""))
 	S.session match {
      case Full(session) => session.setupComet("PostActor",name,tagObj)
      case Empty => S.error("no session!")
      case Failure(msg,_,_) => S.error("no session!"+msg)
    }
    tempalte
  }
	
/*	def popularTag =  ".tag-list *" #> 
	{ 
	  ".tag-item" #> (SnippetTags.getTopTag(5).flatMap( p => 
	    ".tag-item" #> ""
	    ))
	    
	}*/
	  

	def popularTag = {
	  val count = S.attr("count",_.toInt).openOr(6)
	  ".tag-list *" #> { 
	     ".tag-all *" #> getTagButton("All") &
	  ".tag-item" #> (SnippetTags.getTopTag(count).map(p =>
    	".tag-item *" #> getTagButton(p.name.get )))}    
}
	    
	def getTagButton(str:String) ={
	  if (str == "All")
		  SHtml.ajaxButton(str,() => sendTagMsgStr(""))
	  else 
		  SHtml.ajaxButton(str,() => sendTagMsgStr(str))
	}
	
	def sendTagMsgStr(tag:String) ={
	  sendTagMsg(TagModel.find(By(TagModel.name,tag)))
	  Noop
	}  
	  
	def sendTagMsg(tag:Box[TagModel]):Unit ={
		for(session <- S.session)
		{
		  session.sendCometActorMessage("PostActor",Full("PostActor"),tag)
		}
	  }
}
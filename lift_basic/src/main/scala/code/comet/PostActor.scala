package code.comet
import net.liftweb.http.CometActor
import net.liftweb.http.CometListener
import code.model.CodeSnippet
import net.liftweb.actor.LiftActor
import net.liftweb.http.ListenerManager
import scala.xml.NodeSeq
import code.model.User
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.common.Failure
import net.liftweb.util._
import net.liftweb.util.BindHelpers
import net.liftweb.util.BindPlus._
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import net.liftweb.util.BindPlus._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import code.model.User
import code.model.CodeSnippet
import omniauth.Omniauth
import dispatch._
import xml.{Text, NodeSeq}
import oauth._
import oauth.OAuth._
import xml.{Text, NodeSeq}
import omniauth.Omniauth
import dispatch._
import oauth.{Token, Consumer}
import oauth._
import oauth.OAuth._
import xml.{Text, NodeSeq}
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http._
import net.liftweb.util.Props
import net.liftweb.sitemap.{Menu, Loc, SiteMap}
import Loc._
import omniauth.AuthInfo
import net.liftweb.json._
import code.snippet.PostForm

class PostActor extends CometActor with CometListener {
//    implicit val formats = net.liftweb.json.DefaultFormats
//    
//		def Content(xhtml : NodeSeq)  = {
//		val user = User.currentUser
//		val posts = user match {
//		  case Full(user) => user.AllPost
//		  case Empty =>  CodeSnippet.findAll()
//		  case Failure(msg,_,_) => List()
//		  
//		}
//		System.out.println("======>"+posts)
//		def bindText(template : NodeSeq) : NodeSeq =
//		{
//		  posts.flatMap{ case (code) => bind("content",template,"text" ->  scala.xml.Unparsed(code.content.get).toString())}
//		  
//		}
//		bind("content",xhtml,"code"->bindText _)
//	}
  
//	private var posts : List[CodeSnippet] = Nil
	private var posts : List[CodeSnippet] = Nil
	
	def registerWith = PostServer
	
	private def renderMessages = <div>{posts.reverse.map(m => <li>{m.content}</li>)}</div>
	
  	  def render = 	bind("chat","input" -> SHtml.ajaxForm(SHtml.text("",sendMessage _)), "messages" -> renderMessages)
  
 private def sendMessage(msg: String) ={
  	    val snippet = CodeSnippet.create
  	    snippet.content.set(msg)
  	    snippet.save
  	    PostServer ! snippet
  	  } 
    
	override def lowPriority = {
	case msg:List[CodeSnippet] => posts = msg;
		reRender()
	}
  
}

object PostServer extends LiftActor with ListenerManager {
  var posts: List[CodeSnippet] = CodeSnippet.findAll()
  def createUpdate = posts
  override def lowPriority = {
	case msg: CodeSnippet => {
		posts ::= msg
		updateListeners()
	}
  }
}
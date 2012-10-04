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
import net.liftweb.util.Helpers._
import code.model.Tag
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.mapper.By
import code.snippet.Post
import net.liftweb.common.Failure
import net.liftweb.common.Box
import code.model.SnippetTags

class PostActor extends CometActor with CometListener {

  
  implicit val formats = net.liftweb.json.DefaultFormats

  private var content = ""
  private var tags = ""
    
  private var posts = getPosts(Empty)
 

  def getPosts(tag:Box[Tag]):List[CodeSnippet] = {
	    tag match {
	    	case Full(theTag) => theTag.posts.all
	    	case Empty =>  CodeSnippet.findAll()
	 	}
  }
/*  def getPosts: List[CodeSnippet] = tagVar.is match {
    case Empty => {
      Console.println("===>var = none")
      CodeSnippet.findAll()
    }
    
    case Full(text) =>{ 
    	Console.println("===>var = "+text)
    	getPost(text)
    }
    case Failure(msg,_,_) => {
      Console.println("===>var = fail")
      S.error(msg)
      CodeSnippet.findAll()
    }
  }*/

  def registerWith = PostServer

  def render = "#postForm" #> ajaxForm & "#postTemplate" #> bindText

  def bindText ={
    ".post_content" #> (
      (ns: NodeSeq) => (posts.flatMap( p => (".content" #> scala.xml.Unparsed(p.content.get) & ".tag *" #> ("Tags:" + p.getTags))(ns))))
  }

  def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd, 
      (SHtml.textarea("", content = _, "id" -> "snippetTextArea") 
    		  ++ SHtml.text("Lift",tags = _)
    		  ++ SHtml.submitButton(() => {})
    		  ++ SHtml.hidden(() => postForm)
      ))
  
  private def postForm = {
    val snippet = CodeSnippet.create
    snippet.content.set(content)
    snippet.tags ++= Tag.getTagList(tags)  
    snippet.save
    PostServer ! snippet
  }

  private def sendMessage(msg: String) = {
    val snippet = CodeSnippet.create
    snippet.content.set(msg)
    snippet.save
    PostServer ! snippet
  }

  override def lowPriority = {
    case msg: List[CodeSnippet] =>
      posts = msg
      reRender(false)
    case msg: CodeSnippet =>
      posts = msg :: posts
      reRender(false)
    case msg: Box[Tag] =>{
      Console.println("=========comet>"+msg.openOr(""))
      posts = getPosts(msg)
      reRender()
    }
  }
}

object PostServer extends LiftActor with ListenerManager {
  var posts: List[CodeSnippet] = CodeSnippet.findAll()
  def createUpdate = posts
  override def lowPriority = {
    case msg: CodeSnippet => {
    	updateListeners(msg)
    }
  }
}
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

class PostActor extends CometActor with CometListener {
    implicit val formats = net.liftweb.json.DefaultFormats
    

  
	private var posts : List[CodeSnippet] = Nil
	
	def registerWith = PostServer
		

    def render = bind("content","template" -> bindText _) 

  	def bindText(template : NodeSeq) : NodeSeq ={
		  posts.flatMap{ case (code) 
		    => BindHelpers.bind("content",template, BindHelpers.TheBindParam("post",scala.xml.Unparsed(code.content.get)))
		  }
	}


  	  
    private def sendMessage(msg: String) ={
  	    val snippet = CodeSnippet.create
  	    snippet.content.set(msg)
  	    snippet.save
  	    PostServer ! snippet
  	  } 
    
	override def lowPriority = {
	case msg:List[CodeSnippet] => posts = msg
		reRender(false)
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
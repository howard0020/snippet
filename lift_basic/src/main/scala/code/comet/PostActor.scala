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

class PostActor extends CometActor with CometListener {
  implicit val formats = net.liftweb.json.DefaultFormats

  private var content = ""
  private var tags = ""
  var param = Full("Lift")
  private var posts: List[CodeSnippet] = param match {
    //case Empty => CodeSnippet.findAll()
    case Full(text) => Tag.find(By(Tag.name,text)).get.posts.toList
   // case Failure(msg,_,_) => {
    //  S.error(msg)
   //   CodeSnippet.findAll()
    //  }
  }

  def registerWith = PostServer

  def render = "#postForm" #> ajaxForm & "#postTemplate" #> bindText

  def bindText =
    ".post_content" #> (
      (ns: NodeSeq) => (posts.flatMap( p => (".content" #> scala.xml.Unparsed(p.content.get) & ".tag *" #> ("Tags:" + p.getTags))(ns))))

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
  }
}

object PostServer extends LiftActor with ListenerManager {
  var posts: List[CodeSnippet] = CodeSnippet.findAll()
  def createUpdate = posts
  override def lowPriority = {
    case msg: CodeSnippet => {
      posts = CodeSnippet.findAll()
      updateListeners()
    }
  }
}